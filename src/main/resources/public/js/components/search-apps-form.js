import React from "react";
import PropTypes from 'prop-types';
import customFetch from "../util/custom-fetch";
import GeoAreaAutosuggest from "./autosuggests/geoarea-autosuggest";
import LabelSection from "./label-section";
import PillInputButton from './pill-input-button';
import UserService from '../util/user-service';

import { i18n } from "../config/i18n-config"
import { t } from "@lingui/macro"


export default class SearchAppsForm extends React.Component {

    state = {
        languages: [],
        selectedLanguage: '',
        selectedOrganizationId: '',
        installStatus: '',
        geoArea: '',
        organizations: null,
        payment: {
            free: false,
            paid: false
        },
        audience: {
            publicbodies: false,
            citizens: false,
            companies: false

        }

    };

    constructor(props){
        super(props);
        this._userService = new UserService();

    }

    componentDidMount = async () =>  {
        this._initializeFilters(this.props.filters);
        await this.initializeLanguage();

        //organization input is just available when user is connected
        const userInfo = await this._userService.fetchUserInfos();
        if(userInfo){
            customFetch("/my/api/organization").then((organizations) => {
                organizations.unshift({name:i18n._(t`store.language.all`), id: ''});
                this.setState({organizations: organizations});
            });
        }
    };

    componentWillReceiveProps(nextProps){
        const {filters} =  nextProps;

        if(filters){
            this._initializeFilters(filters);
        }
    }

    _initializeFilters = (filters) => {
        let {audience, payment, geoArea,selectedOrganizationId, selectedLanguage} = this.state;

            for(let key in audience){
                audience[key] = filters.audience[key];
            }
            for(let key in payment){
                payment[key] = filters.payment[key];
            }
            selectedOrganizationId = filters.selectedOrganizationId;
            geoArea = filters.geoArea;
            selectedLanguage = filters.selectedLanguage;

            this.setState({
                audience,
                payment,
                selectedOrganizationId,
                geoArea,
                selectedLanguage
            });
    };

    initializeLanguage = async () => {
        const {config} = this.props;
        let languages = Object.assign([], config.languages);
        languages.unshift('all');
        if(this.props.filters.selectedLanguage === '') {
            this.setState({languages: languages, selectedLanguage: config.language});
            this.props.updateFilter(null, "selectedLanguage", config.language);
        }else{
            this.setState({languages: languages});
        }
    };

    resetFilters = () => {
        this.setState({
            selectedLanguage: '',
            selectedOrganizationId: '',
            geoArea: '',
            payment: {
                free: false,
                paid: false
            },
            audience: {
                publicbodies: false,
                citizens: false,
                companies: false

            }});
    };

    _handleLanguageClicked = (event) => {
        const selectedLanguage = event.target.value;
        this.setState({selectedLanguage: selectedLanguage});
        this.props.updateFilter(null, "selectedLanguage", selectedLanguage);
    };

    _handleGeoSelected = (event, value) => {
        this.props.updateFilter(null, "geoArea", value);
    };

    _handleGeoChange = (event, value) => {
        let {geoArea} = this.state;
        geoArea.name = value;
        this.setState({geoArea});
        if(value===''){
            this.props.updateFilter(null, "geoArea", '');
        }
    };

    _handleOrganizationChange = (event) => {
        const selectedOrganizationId = event.target.value;
        this.setState({selectedOrganizationId: selectedOrganizationId});
        this.props.updateFilter(null, "selectedOrganizationId", selectedOrganizationId);
    };

    _handleInstallStatusChange = (event) => {
        const installStatus = event.target.value;
        console.log(installStatus);
        this.setState({installStatus: installStatus});
        this.props.updateFilter(null, "installStatus", installStatus);
    };

    _handleOnPaymentChange = (event) => {
        const inputModified = event.target.name;
        let {payment} = this.state;
        payment[inputModified] = event.target.checked;
        this.setState({payment: payment});

        this.props.updateFilter("payment", inputModified, event.target.checked);
    };
    _handleAudienceChange = (event) => {
        const inputModified = event.target.name;
        let {audience} = this.state;
        audience[inputModified] = event.target.checked;
        this.setState({audience: audience});

        this.props.updateFilter("audience", inputModified, event.target.checked);
    };

    render() {
        const {languages, selectedLanguage, payment, audience, selectedOrganizationId, organizations, installStatus} = this.state;
        const languageComponents = languages.map(language =>
            <option key={language} value={language}>{i18n._(`store.language.${language}`)}</option>
        );
        const organizationComponents = organizations ? organizations.map(organization =>
            <option key={organization.id} value={organization.id}>{organization.name}</option>
        ) : null;

        return (
            <div id="search-apps-form">
                {/*LANGUAGE*/}
                <LabelSection label={i18n._('store.languages-supported-by-applications')}>
                    <select id="language" className="form-control"
                            onChange={this._handleLanguageClicked}
                            value={selectedLanguage}>
                        {languageComponents}
                    </select>
                </LabelSection>
                {/*GEOAREA*/}
                <LabelSection label={i18n._('store.geoarea')}>
                    <GeoAreaAutosuggest name="geoSearch"
                                        countryUri=""
                                        endpoint="areas"
                                        onChange={this._handleGeoChange}
                                        onGeoAreaSelected={this._handleGeoSelected}
                                        value={this.state.geoArea.name}
                    />
                </LabelSection>
                {/*ORGANIZATION*/}
                {organizations &&
                    <LabelSection label={i18n._('organization.search.title')}>
                        <select id="organization" className="form-control"
                                onChange={this._handleOrganizationChange}
                                value={selectedOrganizationId}>
                            {organizationComponents}
                        </select>
                    </LabelSection>
                }
                {/*INSTALLED*/}
                {selectedOrganizationId &&
                    <LabelSection label={i18n._('my.apps.status')}>
                        <select id="installed" className="form-control"
                                onChange={this._handleInstallStatusChange}
                                value={installStatus}>
                            <option value={''}>{i18n._('all')}</option>
                            <option value={"installed"}>{i18n._('installed')}</option>
                            <option value={"not_installed"}>{i18n._('not-installed')}</option>
                        </select>
                    </LabelSection>
                }
                {/*MODE*/}
                <LabelSection label={i18n._('store.mode')}>
                    <PillInputButton label={i18n._('store.free')} id={"free-checkbox"}
                                checked={payment.free} name={'free'} onChange={this._handleOnPaymentChange}/>

                    <PillInputButton label={i18n._('store.paid')} id={"paid-checkbox"}
                                checked={payment.paid} name={'paid'} onChange={this._handleOnPaymentChange}/>

                </LabelSection>
                {/*AUDIENCE*/}
                <LabelSection label={i18n._('store.audience')}>
                    <PillInputButton label={i18n._('store.citizens')} id={"citizens-checkbox"}
                                checked={audience.citizens} name={'citizens'} onChange={this._handleAudienceChange}/>

                    <PillInputButton label={i18n._('store.publicbodies')} id={"publicbodies-checkbox"}
                                checked={audience.publicbodies} name={'publicbodies'} onChange={this._handleAudienceChange}/>

                    <PillInputButton label={i18n._('store.companies')} id={"companies-checkbox"}
                                checked={audience.companies} name={'companies'} onChange={this._handleAudienceChange}/>
                </LabelSection>


            </div>

        )
    }


}

SearchAppsForm.propTypes = {};
