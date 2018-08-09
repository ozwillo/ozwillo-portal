'use strict';

import React from 'react';
import PropTypes from "prop-types";
import {connect} from 'react-redux';
import createClass from 'create-react-class';

import GeoAreaAutosuggest from '../components/autosuggests/geoarea-autosuggest';
import AppModal from '../components/store-install-app'
import UpdateTitle from '../components/update-title';


const AppStore = createClass({
    getInitialState: function () {
        return {
            defaultApp: null,
            apps: [],
            loading: true,
            maybeMoreApps: false,
            filter: {
                audience: {
                    citizens: true,
                    publicbodies: true,
                    companies: true
                },
                payment: {
                    paid: true,
                    free: true
                },
                selectedLanguage: this.props.config.language,
                geoAreaAncestorsUris: [],
                searchText: ''
            }
        };
    },
    componentDidMount() {
        this.updateApps();
    },
    componentWillReceiveProps: function (nextProps) {
        if (nextProps.defaultApp && (!this.state.defaultApp || this.state.defaultApp.id != nextProps.defaultApp.id)) {
            $.ajax({
                url: "/api/store/application/" + nextProps.defaultApp.type + "/" + nextProps.defaultApp.id,
                type: 'get',
                dataType: 'json',
                success: function (data) {
                    const state = this.state;
                    state.defaultApp = data;
                    state.defaultApp.isDefault = true; // triggers opening modal
                    this.setState(state);
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error(status, err.toString());
                }.bind(this)
            });
        }
    },
    updateFilter: function (category, key, value) {
        const filter = this.state.filter;
        if (category) {
            const filterCategory = filter[category];
            filterCategory[key] = value;
        } else {
            filter[key] = value;
        }
        this.setState({filter: filter});
        this.updateApps();
    },
    getSearchFilters: function () {
        const supported_locales = [];
        if (this.state.filter.selectedLanguage !== 'all') {
            supported_locales.push(this.state.filter.selectedLanguage);
        }
        const filter = this.state.filter;
        return {
            target_citizens: filter.audience.citizens,
            target_publicbodies: filter.audience.publicbodies,
            target_companies: filter.audience.companies,
            free: filter.payment.free,
            paid: filter.payment.paid,
            supported_locales: supported_locales,
            geoArea_AncestorsUris: filter.geoAreaAncestorsUris,
            category_ids: [],
            q: filter.searchText  // q being empty is ok
        };
    },
    updateApps: function () {
        $.ajax({
            url: "/api/store/applications",
            data: this.getSearchFilters(),
            traditional: true, // else geographical_areas%5B%5D=http... see http://stackoverflow.com/questions/6011284/problem-with-brackets-in-jquery-form-data-when-sending-data-as-json
            type: 'get',
            dataType: 'json',
            success: function (data) {
                this.setState({
                    apps: data.apps,
                    maybeMoreApps: data.maybeMoreApps,
                    loading: false
                });
            }.bind(this),
            error: function (xhr, status, err) {
                this.setState({apps: [], loading: false});
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    loadMoreApps: function () {
        this.setState({loading: true});

        const searchFilters = this.getSearchFilters();
        searchFilters.last = this.state.apps.length;
        $.ajax({
            url: "/api/store/applications",
            data: searchFilters,
            type: 'get',
            dataType: 'json',
            success: function (data) {
                const state = this.state;
                state.apps = state.apps.concat(data.apps);
                state.loading = false;
                state.maybeMoreApps = data.maybeMoreApps;
                this.setState(state);
            }.bind(this),
            error: function (data) {
                // maybe not such an error, could be there's no more data to get...
                const state = this.state;
                state.loading = false;
                state.maybeMoreApps = false;
                this.setState(state);
            }.bind(this)
        });
    },
    mergeAppsWithDefaultAppFirst: function () {
        if (!this.state.defaultApp) {
            return this.state.apps;
        }

        const apps = this.state.apps.map(app => {
            return (app.id === this.state.defaultApp.id) ? this.state.defaultApp : app;
        });

        return apps;
    },
    render: function () {
        const languagesAndAll = Object.assign([], this.props.config.languages);
        languagesAndAll.unshift('all');

        return (
            <section id="store">
                <SearchAppsForm ref="searchAppsForm"
                                languages={languagesAndAll}
                                updateApps={this.updateApps}
                                filter={this.state.filter}
                                updateFilter={this.updateFilter}/>
                <AppList apps={this.mergeAppsWithDefaultAppFirst()}/>
                <LoadMore
                    loading={this.state.loading}
                    maybeMoreApps={this.state.maybeMoreApps}
                    loadMoreApps={this.loadMoreApps}/>
            </section>
        );
    }
});
AppStore.contextTypes = {
    t: PropTypes.func.isRequired
};

const mapStateToProps = state => {
    return {
        config: state.config
    };
};
const AppStoreWithRedux = connect(mapStateToProps)(AppStore)


const SearchAppsForm = createClass({
    getInitialState () {
        return {
            geoArea: ''
        };
    },
    handleLanguageClicked: function (event) {
        this.props.updateFilter(null, "selectedLanguage", event.target.value);
    },
    fullTextSearchChanged: function (event) {
        this.props.updateFilter(null, "searchText", event.target.value);
    },
    onGeoSelected: function(event, value){
        this.props.updateFilter(null, "geoAreaAncestorsUris", value.ancestors);
    },
    onGeoChange: function (event, value) {
        this.setState({geoArea: value})
    },
    onAudienceChange: function (event) {
        this.props.updateFilter("audience", event.target.name, event.target.checked);
    },
    onPaymentChange: function (event) {
        this.props.updateFilter("payment", event.target.name, event.target.checked);
    },
    render: function () {
        const languageComponents = this.props.languages.map(language =>
            <option key={language} value={language}>{this.context.t(`store.language.${language}`)}</option>
        );

        return (
            <section className="box">
                <div className="row form-horizontal" id="store-search">
                    <div className="col-md-6">
                        <div className="form-group">
                            <label htmlFor="language"
                                   className="col-sm-4 control-label">{this.context.t('languages-supported-by-applications')}</label>

                            <div className="col-sm-8">
                                <select id="language" className="form-control" onChange={this.handleLanguageClicked}
                                        value={this.props.filter.selectedLanguage}>
                                    {languageComponents}
                                </select>
                            </div>
                        </div>

                        {/* geo-filer - filtering by jurisdiction (geoArea) */}
                        <div className="form-group">
                            <label htmlFor="geoSearch"
                                   className="col-sm-4 control-label">{this.context.t('geoarea')}</label>

                            <div className="col-sm-8">
                                <GeoAreaAutosuggest name="geoSearch"
                                                    countryUri=""
                                                    endpoint="areas"
                                                    onChange={this.onGeoChange}
                                                    onGeoAreaSelected={this.onGeoSelected}
                                                    value={this.state.geoArea}
                                                    />
                            </div>
                        </div>
                        <div className="form-group">
                            <label className="col-sm-4 control-label">{this.context.t('mode')}</label>

                            <div className="col-sm-8">
                                <label className="checkbox-inline">
                                    <input type="checkbox" name="free" checked={this.props.filter.payment.free}
                                           onChange={this.onPaymentChange}/>{this.context.t('free')}
                                </label>
                                <label className="checkbox-inline">
                                    <input type="checkbox" name="paid" checked={this.props.filter.payment.paid}
                                           onChange={this.onPaymentChange}/>{this.context.t('paid')}
                                </label>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-6 right">
                        <div className="form-group">
                            <label htmlFor="fulltext"
                                   className="col-sm-4 control-label">{this.context.t('keywords')}</label>

                            <div className="col-sm-8">
                                <input type="text" id="fulltext" className="form-control"
                                       onChange={this.fullTextSearchChanged}
                                       placeholder={this.context.t('keywords')} name="fullTextSearch"/>
                            </div>
                        </div>
                        <div className="form-group">
                            <label className="col-sm-4 control-label">{this.context.t('audience')}</label>

                            <div className="col-sm-8">
                                <div className="checkbox">
                                    <label>
                                        <input type="checkbox" name="citizens"
                                               checked={this.props.filter.audience.citizens}
                                               onChange={this.onAudienceChange}/>{this.context.t('citizens')}
                                    </label>
                                </div>
                                <div className="checkbox">
                                    <label>
                                        <input type="checkbox" name="publicbodies"
                                               checked={this.props.filter.audience.publicbodies}
                                               onChange={this.onAudienceChange}/>{this.context.t('publicbodies')}
                                    </label>
                                </div>
                                <div className="checkbox">
                                    <label>
                                        <input type="checkbox" name="companies"
                                               checked={this.props.filter.audience.companies}
                                               onChange={this.onAudienceChange}/>{this.context.t('companies')}
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        );
    }
});
SearchAppsForm.contextTypes = {
    t: PropTypes.func.isRequired
};

const AppList = createClass({
    renderApps: function () {
        return this.props.apps.map(function (app) {
            return (
                <App key={app.id} app={app}/>
            );
        });
    },
    render: function () {
        if (this.props.apps.length === 0)
            return (<div></div>);
        else
            return (
                <div className="container-fluid">
                    <div className="row" id="store-apps">
                        <div className="col-md-12">
                            <div className="row clearfix">
                                {this.renderApps()}
                            </div>
                        </div>
                    </div>
                </div>
            );
    }
});

const LoadMore = createClass({
    renderLoading: function () {
        if (this.props.loading) {
            return (
                <div className="text-center">
                    <i className="fa fa-spinner fa-spin loading"></i> {this.context.t('ui.loading')}
                </div>
            );
        } else if (this.props.maybeMoreApps) {
            return (
                <div className="text-center">
                    <button className="btn btn-lg btn-default"
                            onClick={this.props.loadMoreApps}>{this.context.t('load-more')}</button>
                </div>
            );
        }
    },
    render: function () {
        if (!this.props.loading && !this.props.maybeMoreApps)
            return (<div></div>);
        else
            return (
                <div className="container-fluid">
                    <div className="row" id="store-loadmore">
                        <div className="col-md-12">
                            {this.renderLoading()}
                        </div>
                    </div>
                </div>
            );
    }
});
LoadMore.contextTypes = {
    t: PropTypes.func.isRequired
};

const App = createClass({
    getInitialState: function () {
        return {
            isOpen: false
        };
    },
    componentWillReceiveProps: function (nextProps) {
        if (!this.state.isOpen && nextProps.app.isDefault) {
            this.openApp();
            this.setState({ isOpen: true });
        }
    },
    openApp: function () {
        this.modal.open();
    },
    render: function () {
        const indicatorStatus = this.props.app.installed ? "installed" : (this.props.app.paid ? "paid" : "free");
        const pubServiceIndicator = this.props.app.public_service ?
            <div className="public-service-indicator">
                <div className="triangle"/>
                <div className="label">
                    <i className="triangle fas fa-university" />
                </div>
            </div> : null;

        return (
            <div className="col-lg-2 col-md-3 col-sm-6 col-xs-12 container-app">
                <AppModal app={this.props.app} wrappedComponentRef={
                    c => { this.modal = c && c.getWrappedInstance() }}/>
                <div className="app">
                    <div className="logo">
                        <img src={this.props.app.icon}/>
                    </div>
                    <div className="description" onClick={this.openApp}>
                        {pubServiceIndicator}
                        <div className="app-header">
                            <span className="app-name">{this.props.app.name}</span>
                            <p className="app-provider">{this.props.app.provider}</p>
                        </div>
                        <p className="app-description">{this.props.app.description}</p>
                        <Indicator status={indicatorStatus}/>
                    </div>
                </div>
            </div>
        );
    }
});

const Indicator = createClass({
    render: function () {
        let btns;
        const status = this.props.status;
        if (status === "installed") {
            btns = [
                <button type="button" key="indicator_button"
                        className="btn btn-lg btn-installed">{this.context.t('installed')}</button>,
                <button type="button" key="indicator_icon" className="btn btn-lg btn-installed-indicator">
                    <i className="fa fa-check" />
                </button>
            ];
        } else if (status === "free") {
            btns = [
                <button type="button" key="indicator_button"
                        className="btn btn-lg btn-free">{this.context.t('free')}</button>,
                <button type="button" key="indicator_icon" className="btn btn-lg btn-free-indicator">
                    <i className="fa fa-gift" />
                </button>
            ];
        } else {
            btns = [
                <button type="button" key="indicator_button"
                        className="btn btn-lg btn-buy">{this.context.t('paid')}</button>,
                <button type="button" key="indicator_icon" className="btn btn-lg btn-buy-indicator">
                    <i className="fa fas fa-euro-sign" />
                </button>
            ];
        }

        return (
            <div className="app-status text-center">
                {btns}
            </div>
        );
    }
});
Indicator.contextTypes = {
    t: PropTypes.func.isRequired
};

import { withRouter } from 'react-router';

class AppStoreWrapper extends React.Component {
    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            defaultApp: null
        };
    }

    componentWillReceiveProps(nextProps) {
        let defaultApp = null;
        const urlParams = nextProps.match.params;

        if (urlParams.id) {
            defaultApp = { type: urlParams.type, id: urlParams.id }
        }

        this.setState({ defaultApp });
    }

    render() {
        return <div className="oz-body wrapper">
            <UpdateTitle title={this.context.t('ui.appstore')}/>

            <header className="title">
                <span>{this.context.t('ui.appstore')}</span>
            </header>

            <AppStoreWithRedux defaultApp={this.state.defaultApp} />
            <div className="push"/>
        </div>;
    }
}

export default withRouter(AppStoreWrapper);
