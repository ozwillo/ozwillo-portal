import React from "react";
import App from "../components/application";
import customFetch from "../util/custom-fetch";
import FilterApp from "../model/filter-app";
import SideNav from "../components/side-nav";
import SearchAppForm from "../components/search-apps-form";
import CustomTooltip from "../components/custom-tooltip";
import PropTypes from "prop-types";
import {i18n} from "../config/i18n-config";
import swal from "sweetalert2";
import {fetchUserInfo} from "../actions/user";
import { t, Trans } from "@lingui/macro"
import UpdateTitle from '../components/update-title';

export default class AppStore extends React.Component {

    constructor() {
        super();
    }

    state = {
        filters: new FilterApp(),
        loading: true,
        moreAppsLoading: false,
        maybeMoreApps: false,
        activeFiltersNumber: 0,
        isSearchBarVisible: "fix",
        scrollValue: 0,
        config: {},
        apps: []
    };

    componentDidMount = async () => {
        //check if the user is connected
        const user = await fetchUserInfo();
        if (user) {
            this._askForFilters();
        } else {
            this.initialize();
        }
    };

    componentWillUnmount() {
        if (location.href.match("store")) {
            localStorage.setItem("askFilterPermission", 'false')
        } else {
            localStorage.setItem("askFilterPermission", 'true')
        }
    }

    _askForFilters = async () => {
        const potentialOldFilters = this._getFiltersFromLocalStorage();
        const askFilterPermission = JSON.parse(localStorage.getItem("askFilterPermission"));
        let numberPotentialActiveFilters = 0;
        if (potentialOldFilters) {
            const filters = this._transformSearchFilters(potentialOldFilters);
            numberPotentialActiveFilters = this._countActiveFilters(filters);
        }

        await this.initialize();


        if (potentialOldFilters && !askFilterPermission) {
            this.setState({filters: potentialOldFilters}, () => {
                this._setFiltersInLocalStorage(potentialOldFilters);
                this.setState({activeFiltersNumber: numberPotentialActiveFilters});
                this._getApps();
            });
        } else if (potentialOldFilters && askFilterPermission && numberPotentialActiveFilters > 0) {
            swal({
                title: i18n._(t`store.apply-old-filters`),
                type: 'info',
                showCancelButton: true,
                cancelButtonText: i18n._(t`ui.cancel`),
                confirmButtonText: i18n._(t`ui.yes`)
            }).then((result) => {
                if (result.value) {
                    this.setState({filters: potentialOldFilters}, () => {
                        this._setFiltersInLocalStorage(potentialOldFilters);
                        this.setState({activeFiltersNumber: numberPotentialActiveFilters});
                        this._getApps();
                    });
                } else {
                    //reset local storage
                    this._setFiltersInLocalStorage(new FilterApp());
                }
            })
        }
    };

    initialize = async () => {
        await this._fetchConfig();
        await this._getApps();
    };

    _fetchConfig = async () => {
        await customFetch('/api/config').then(
            config => {
                this.setState({config: config})
            }
        )
    };

    updateFilters = (category, key, value) => {
        const filters = this.state.filters;
        if (category) {
            const filterCategory = filters[category];
            filterCategory[key] = value;
        } else {
            filters[key] = value;
        }
        this.setState({filters: filters});
        this._setFiltersInLocalStorage(filters);
        this._getApps();
    };

    _handleFullTextSearchChanged = (event) => {
        this.updateFilters(null, "searchText", event.target.value);
    };

    _transformSearchFilters = (filters) => {
        const supported_locales = [];
        if (filters && filters.selectedLanguage !== 'all') {
            supported_locales.push(filters.selectedLanguage);
        }
        return {
            target_citizens: filters.audience.citizens,
            target_publicbodies: filters.audience.publicbodies,
            target_companies: filters.audience.companies,
            free: filters.payment.free,
            paid: filters.payment.paid,
            supported_locales: supported_locales,
            organizationId: filters.selectedOrganizationId,
            installed_status: filters.installStatus,
            geoArea_AncestorsUris: filters.geoArea.ancestors,
            category_ids: [],
            q: filters.searchText,
            last: filters.last
        };
    };

    _setFiltersInLocalStorage = (filters) => {
        localStorage.setItem('filters', JSON.stringify(filters));
    };

    _getFiltersFromLocalStorage = () => {
        return JSON.parse(localStorage.getItem('filters'));
    };

    _countActiveFilters = (filters) => {
        let counter = 0;
        for (let key in filters) {
            let elem = filters[key];
            //exclude from the filter count the query "q"
            if ((elem && Array.isArray(elem) && elem.length > 0)
                || (key !== "q" && elem && elem !== "" && !Array.isArray(elem))) {
                counter++;
            }
        }
        return counter;
    };

    _resetFilters = () => {
        const cleanFilters = new FilterApp();
        this.setState({filters: cleanFilters}, () => {
            this._setFiltersInLocalStorage(cleanFilters);
            this.refs['searchAppForm'].resetFilters();
            this.initialize();
        });
    };

    _getApps = async () => {
        const filters = this._transformSearchFilters(this.state.filters);
        const numberActiveFilters = this._countActiveFilters(filters);
        this.setState({activeFiltersNumber: numberActiveFilters});
        try {
            const res = await customFetch(`/api/store/applications`, {urlParams: filters});
            this.setState({
                apps: res.apps,
                maybeMoreApps: res.maybeMoreApps,
                loading: false
            });
        } catch (err) {
            this.setState({apps: [], loading: false});
            console.error(err.toString());
        }
    };

    _loadMoreApps = async () => {
        const {apps} = this.state;
        this.setState({moreAppsLoading: true});

        const filters = this._transformSearchFilters(this.state.filters);
        filters.last = apps.length;

        try {
            const res = await customFetch(`/api/store/applications`, {urlParams: filters});
            this.setState({
                apps: apps.concat(res.apps),
                maybeMoreApps: res.maybeMoreApps,
                moreAppsLoading: false
            });
        } catch (err) {
            console.error(err.toString())
        }
    };

    _displayLoadMore = () => {
        const {moreAppsLoading, maybeMoreApps} = this.state;
        return (
            <div className={"load-more-apps"}>
                {moreAppsLoading && maybeMoreApps ?
                    <div className="text-center">
                        <i className="fa fa-spinner fa-spin loading"/> {i18n._(t`ui.loading`)}
                    </div>
                    : (
                        maybeMoreApps ? <div className="text-center">
                            <button className="btn btn-lg btn-default"
                                    onClick={this._loadMoreApps}>{i18n._(t`store.load-more`)}</button>
                        </div> : null
                    )
                }
            </div>

        )
    };

    _displayApps = () => {
        const {apps} = this.state;
        return apps
            .map((app) => {
                return (
                    <App key={app.id} app={app} config={this.state.config}/>
                );
            });

    };

    render() {
        this.cancel = i18n._(t`ui.cancel`);
        const {loading, activeFiltersNumber, config, filters, maybeMoreApps, moreAppsLoading} = this.state;
        const filterCounter = activeFiltersNumber > 0 &&
            <div className={"badge-filter-close"}>
                <CustomTooltip title={i18n._(t`active-filters`)}>{activeFiltersNumber}</CustomTooltip>
            </div>;
        const filterCounterHeader = activeFiltersNumber > 0 &&
            <React.Fragment>
                <CustomTooltip title={i18n._(t`reset-filters`)}>
                    <i className={"reset-filters fa fa-trash"} onClick={this._resetFilters}/>
                </CustomTooltip>
                <div className={"active-filters"}><Trans>Active filters</Trans> :</div>
                <div className={"badge-filter-open"}>
                    <CustomTooltip title={i18n._(t`active`)}>
                        {activeFiltersNumber}
                    </CustomTooltip>
                </div>
            </React.Fragment>;


        return (
            <React.Fragment>
                <UpdateTitle title={i18n._(t`ui.appstore`)}/>
                {loading ?
                    <div className={"app-store-wrapper"}>
                        <div className="app-store-container-loading text-center">
                            <i className="fa fa-spinner fa-spin loading"/>
                        </div>
                    </div>
                    :
                    <div className={"app-store-wrapper oz-body"}>
                        <SideNav isCloseChildren={filterCounter} isOpenHeader={filterCounterHeader}>
                            <SearchAppForm ref={"searchAppForm"} updateFilter={this.updateFilters} config={config}
                                           filters={filters}/>
                        </SideNav>
                        <div className={"app-store-container"} id="store-apps">

                            <header className="title">
                                <span>{i18n._(t`ui.appstore`)}</span>
                            </header>

                            <input type="text" id="fulltext"
                                   className={"form-control search-bar"}
                                   onChange={this._handleFullTextSearchChanged}
                                   value={this.state.filters.searchText}
                                   placeholder={i18n._(t`store.keywords`)} name="fullTextSearch"/>
                            <div className={"app-list"}>
                                {this._displayApps()}
                            </div>
                            {this._displayLoadMore()}
                        </div>
                    </div>
                }
            </React.Fragment>
        )
    }
}
