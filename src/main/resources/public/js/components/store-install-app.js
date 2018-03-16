'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import {connect} from 'react-redux';
import createClass from 'create-react-class';
import PropTypes from 'prop-types'

var Showdown = require('showdown');
var converter = new Showdown.Converter({tables: true});

import {Modal} from './bootstrap-react';
import {RatingWrapper} from './rating';
import GeoAreaAutosuggest from './autosuggests/geoarea-autosuggest';
import {CreateOrModifyOrganizationForm} from './create-or-modify-organization';

var default_org_data = {
    organization: {
        exist: false,
        legal_name: '',
        sector_type: '',
        in_activity: true,
        alt_name: '',
        org_type: '',
        tax_reg_num: '',
        tax_reg_official_id: '',
        tax_reg_activity_uri: '',
        jurisdiction_uri: '',
        jurisdiction: '',
        phone_number: '',
        web_site: '',
        street_and_number: '',
        po_box: '',
        city: '',
        city_uri: '',
        zip: '',
        cedex: '',
        country_uri: ''
    }, errors: [[], []], typeRestriction: ''
};

var AppModal = createClass({
    getInitialState: function () {
        return {
            app: {rating: 0, rateable: true, tos: '', policy: '', longdescription: '', screenshots: null},
            orgs: [],
            selectedOrg: jQuery.extend(true, {}, default_org_data.organization),
            createOrg: false,
            buying: false,
            installing: false,
            isInstalled: false,
            error: false,
            step: 1
        };
    },
    componentDidMount: function () {
        const storeUrl = `/${this.props.config.language}/api/store`;
        $(ReactDOM.findDOMNode(this)).on("hide.bs.modal", function (event) {
            history.pushState({}, "store", storeUrl);
        }.bind(this));
    },
    componentDidUpdate: function () {
        var desc = $(ReactDOM.findDOMNode(this)).find(".app-description table");
        desc.addClass("table table-bordered table-condensed table-striped");
    },
    loadApp: function () {
        $.ajax({
            url: "/api/store/details/" + this.props.app.type + "/" + this.props.app.id,
            type: 'get',
            dataType: 'json',
            success: function (data) {
                var state = this.state;
                state.app = data;
                this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    loadOrgs: function () {
        $.ajax({
            url: "/api/store/organizations/" + this.props.app.type + "/" + this.props.app.id,
            type: 'get',
            dataType: 'json',
            success: function (data) {
                var state = this.state;
                state.orgs = data;
                this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    open: function () {
        this.setState(this.getInitialState());
        const storeUrl = `/${this.props.config.language}/api/store`;
        var href = storeUrl + "/" + this.props.app.type + "/" + this.props.app.id;
        if (typeof history.pushState == "function") {
            history.pushState({}, "application", href);
        }

        this.loadApp();
        const isLogged = !!this.props.userInfo.sub;
        if (isLogged) {
            this.loadOrgs();
        }

        this.refs.modal.open();
    },
    close: function () {
        if (this.state.isInstalled) {
            // redirect to the Dashboard
            window.location = "/my/dashboard";
        }
    },
    doInstallApp: function (organization, updateUserData) {
        var state = this.state;
        state.installing = false;
        state.buying = true; //set it to display the spinner until any below ajax response is received.

        this.setState(state);

        var request;
        if (updateUserData) {
            request = updateUserData;
            request.appId = this.props.app.id;
            request.appType = this.props.app.type;
        }
        else {
            request = {appId: this.props.app.id, appType: this.props.app.type};
        }
        if (organization) {
            request.organizationId = organization;
        }

        $.ajax({
            url: '/api/store/buy',
            type: 'post',
            data: JSON.stringify(request),
            contentType: 'application/json',
            success: function (data) {
                if (data.success) {
                    this.displaySucessfulInstallForm();
                } else {
                    var state = this.state;
                    state.buying = false;
                    state.error = true;
                    this.setState(state);
                }
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
                var state = this.state;
                state.buying = false;
                this.setState(state);
            }.bind(this)
        });

    },
    orgCreated: function (org) {
        var state = this.state;
        state.createOrg = false;
        if (org) {
            this.doInstallApp(org.id);
        }
        this.setState(state);
    },
    cancelCreateOrg: function () {
        var state = this.state;
        state.createOrg = false;
        this.setState(state);
    },
    rateApp: function (rate) {
        $.ajax({
            url: "/api/store/rate/" + this.props.app.type + "/" + this.props.app.id,
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({rate: rate}),
            success: function () {
                var state = this.state;
                state.app.rateable = false;
                state.app.rating = rate;
                this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    displayInstallForm: function () {
        var state = this.state;
        state.installing = true;
        this.setState(state);
    },
    displaySucessfulInstallForm: function () {
        var state = this.state;
        state.buying = false;
        state.isInstalled = true;
        this.setState(state);
    },
    continueInstallProcess: function () { /* set data and display CreateOrgForm OR Install for personal apps */
        var state = this.state;
        state.installType = this.refs.instalForm.getInstallType();

        // preparing data to be transmitted
        var installData = this.refs.instalForm.getInstallData();

        if (state.installType === 'ORG') {

            var orgSearchData = this.refs.instalForm.getOrgSearchData();
            orgSearchData.contact_name = installData.contact.contact_name;
            orgSearchData.contact_lastname = installData.contact.contact_lastname;

            state.selectedOrg.typeInstallOrg = orgSearchData.typeInstallOrg;

            if (orgSearchData.typeInstallOrg === 'NEW-ORGS') {
                $.ajax({
                    url: '/my/api/network/search-organization',
                    type: 'get',
                    contentType: 'json',
                    data: orgSearchData,
                    success: function (data) {
                        // If we received data, it means organization can be declared in the portail
                        if (data) {
                            state.installing = false;
                            state.createOrg = true;
                            state.selectedOrg = data;
                            state.selectedOrg.inModification = false;
                            state.errors = [];
                            this.setState(state);
                        } else {
                            this.refs.modalError.open();
                            this.setState(state);
                        }
                    }.bind(this),
                    error: function (xhr, status, err) {
                        console.error(status, err.toString());
                        var state = this.state;
                        state.errors = ["general"];
                        this.setState(state);
                    }.bind(this)
                });
            } else {
                state.installing = false;
                this.doInstallApp(orgSearchData.selectedOrgId, orgSearchData);
            }
        } else { // PERSONAL
            var orgSearchData = this.refs.instalForm.state.installData.address;//this.refs.addressComponent.props.addressContainer;
            orgSearchData.contact_name = installData.contact.contact_name;
            orgSearchData.contact_lastname = installData.contact.contact_lastname;

            // call method to update data user while calling the Installation Process
            this.doInstallApp(null, orgSearchData);
        }
    },
    orgTypeRestriction: function () {
        return {
            company: this.props.app.target_companies,
            public_body: this.props.app.target_publicbodies
        };
    },
    onStepChange: function (stepId) {
        this.setState({step: stepId});
    },
    renderCreateNewOrganization: function () {
        return (
            <div>
                <h4>{this.context.t('my.network.organization.step') + " " + this.state.step + " / 2"}</h4>
                <CreateOrModifyOrganizationForm ref="tabbedForm"
                                                successHandler={this.orgCreated} cancelHandler={this.cancelCreateOrg}
                                                organization={this.state.selectedOrg}
                                                typeRestriction={this.orgTypeRestriction()}
                                                step={this.state.step} onStepChange={this.onStepChange}
                                                fromStore={true}/>
            </div>
        );
    },
    renderBuying: function () {
        return (<h3><i className="fa fa-spinner fa-spin loading"/> {this.context.t('buying')}</h3>);
    },
    renderInstallingForm: function () {
        return (<InstallForm ref='instalForm'
                             installApp={this.installApp} url={this.state.app.serviceUrl}
                             app={this.props.app} orgs={this.state.orgs}
                             continueInstallProcess={this.continueInstallProcess}
        />);
    },
    renderAppDescription: function () {
        return (
            <AppDescriptionComponentWithRedux app={this.props.app} stateApp={this.state.app}
                                              rateApp={this.rateApp} onInstallButton={this.displayInstallForm}
                                              error={this.state.error}/>
        );
    },
    renderSucessfulInstallationForm: function () {
        return (<div>
                <div className='form-horizontal'>
                    <i id="success-app-install" className="fa fa-check pull-left col-sm-offset-1"></i>
                    <div className='form-group'>
                        <h5 className="col-sm-offset-2">{this.context.t('install.org.success-msg-1')}</h5><br/>
                    </div>
                    {!this.props.app.paid ? '' :
                        <div className='form-group'>
                            <h5 className="col-sm-offset-2">{this.context.t('install.org.success-msg-2')}</h5><br/>
                        </div>
                    }
                    {this.state.installType === 'PERSONAL' ? '' :
                        <div className='form-group'>
                            <h5 className="col-sm-offset-2">{this.context.t('install.org.success-msg-3')}</h5><br/>
                        </div>
                    }
                    <div className='form-group'>
                        <h5 className="col-sm-offset-2">{this.context.t('install.org.success-msg-4')}</h5><br/>
                    </div>
                </div>
            </div>
        );
    },

    render: function () {
        var title = this.props.app.name;
        var content = null;
        if (this.state.buying) {
            content = this.renderBuying();
        } else if (this.state.createOrg) {
            content = this.renderCreateNewOrganization();
            title = (!this.state.selectedOrg.exist ? this.context.t('create-new-org') : this.context.t('modify-org')) + " " + this.state.selectedOrg.legal_name;
        } else if (this.state.installing) {
            content = this.renderInstallingForm();
        } else if (this.state.isInstalled) {
            content = this.renderSucessfulInstallationForm();
        } else {
            content = this.renderAppDescription();
        }

        return (
            <div>
                <Modal ref="modal" large={true} infobox={true} title={title} cancelHandler={this.close}>
                    {content}
                </Modal>
                <Modal ref="modalError" title={this.context.t('ui.something_went_wrong_title')} infobox={true}
                       cancelHandler={null}>
                    <div><h5>{this.context.t('search.organization.cannot-be-used')}</h5></div>
                </Modal>
            </div>);
    }
});
AppModal.contextTypes = {
    t: PropTypes.func.isRequired
};

/** PROPS: app{}, stateApp{}, rateApp(), onInstallButton(), errors[] */
var AppDescriptionComponent = createClass({
    render: function () {
        var stateApp = this.props.stateApp;

        var carousel = (stateApp.screenshots && stateApp.screenshots.length > 0)
            ? (<div className="row">
                <Carousel images={stateApp.screenshots}/>
            </div>)
            : null;

        var error = (this.props.error)
            ? (<div className="alert alert-danger alert-dismissible" role="alert">
                <button type="button" className="close" data-dismiss="alert">
                    <span aria-hidden="true">&times;</span>
                    <span className="sr-only">{this.context.t('ui.close')}</span>
                </button>
                <strong>{this.context.t('sorry')}</strong> {this.context.t('could-not-install-app')}
            </div>)
            : null;

        var rateInfo = null;
        const isLogged = !!this.props.userInfo.sub;
        if (isLogged && !stateApp.rateable) {
            rateInfo = (<p>{this.context.t('already-rated')}</p>);
        }

        var description = converter.makeHtml(stateApp.longdescription);

        var launchOrInstallButton;
        if (this.props.app.type == "service" && this.props.app.installed) {
            if (this.props.stateApp && this.props.stateApp.serviceUrl) {
                launchOrInstallButton =
                    <a className="btn btn-default btn-lg pull-right" href={this.props.stateApp.serviceUrl}
                       target="_new">{this.context.t('launch')}</a>;
            } else {
                launchOrInstallButton = (<label> <i className="fa fa-spinner fa-spin loading"/> </label>);
            }
        } else {
            const storeUrl = `/${this.props.config.language}/api/store`;
            launchOrInstallButton = !isLogged
                ? (<a className="btn btn-default btn-lg pull-right"
                      href={storeUrl + "/login?appId=" + this.props.app.id + "&appType=" + this.props.app.type}>{this.context.t('install')}</a>)
                : (<button type="button" className="btn btn-default btn-lg pull-right"
                           onClick={this.props.onInstallButton}>{this.context.t('install')}</button>)
        }


        return (
            <div className="store-app-card">
                <div className="row">
                    <div className="col-sm-1">
                        <img src={this.props.app.icon} alt={this.props.app.name}/>
                    </div>
                    <div className="col-sm-7">
                        <div>
                            <p>{this.context.t('by')} {this.props.app.provider}</p>
                            <RatingWrapper rating={stateApp.rating} appId={this.props.app.id}
                                           rateable={stateApp.rateable} rate={this.props.rateApp}/>
                            {rateInfo}
                        </div>
                    </div>
                    <div className="col-sm-4">
                        {launchOrInstallButton}
                    </div>
                </div>
                {error}
                {carousel}
                <div className="row">
                    <div className="col-sm-12">
                        <div className="app-description" dangerouslySetInnerHTML={{__html: description}}></div>
                    </div>
                </div>
                <div className="row">
                    <div className="col-sm-12">
                        <div className="app-tos">
                            <p>{this.context.t('agree-to-tos')}</p>
                            <p>
                                <a href={stateApp.tos} target="_new">{this.context.t('tos')}</a>
                            </p>
                            <p>
                                <a href={stateApp.policy} target="_new">{this.context.t('privacy')}</a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});
AppDescriptionComponent.contextTypes = {
    t: PropTypes.func.isRequired
};
const mapStateToProps = state => {
    return {
        config: state.config,
        userInfo: state.userInfo
    };
};
const AppDescriptionComponentWithRedux = connect(mapStateToProps)(AppDescriptionComponent)

// INSTALLATION PROCESS

/** PROPS: app{}, errors[], url, orgs[], continueInstallProcess() */
var InstallForm = createClass({
    getInitialState: function () {
        this.getProfileInfo();
        return ({
            installType: 'PERSONAL',
            installData: {
                contact: {contact_name: '', contact_lastname: ''},
                address: {
                    exist: false,
                    street_and_number: '',
                    city: '',
                    zip: '',
                    country_uri: '',
                    cedex: '',
                    po_box: ''
                }
            },
            errors: []
        })
    },
    getProfileInfo: function () {
        $.ajax({
            url: '/my/api/network/general-user-info',
            type: 'get',
            contentType: 'json',
            success: function (data) {
                var state = this.state;
                state.installData.contact.contact_name = data.user_name;
                state.installData.contact.contact_lastname = data.user_lastname;
                if (data.address) {
                    state.installData.address.street_and_number = data.address.street_address;
                    state.installData.address.city = data.address.locality;
                    state.installData.address.zip = data.address.postal_code;
                    state.installData.address.country = data.address.country;
                }
                this.setState(state);
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(status, err.toString());
            }.bind(this)
        });
    },
    getInstallType: function () {
        var installTypeRestrictions = {personal: this.hasCitizens(), org: this.hasOrganizations()};
        if (this.state.installType === 'PERSONAL' && installTypeRestrictions.personal === false) {
            return 'ORG';
        }
        else if (this.state.installType === 'ORG' && installTypeRestrictions.org === false) {
            return 'PERSONAL';
        }
        else {
            return this.state.installType;
        }
    },
    getInstallData: function () {
        return this.state.installData
    },
    getOrgSearchData: function () {
        return this.refs.setOrgComponent.getOrgSearchData()
    },
    isOnlyForCitizens: function () {
        return this.props.app.target_citizens && !this.props.app.target_companies && !this.props.app.target_publicbodies;
    },
    hasCitizens: function () {
        return this.props.app.target_citizens;
    },
    hasOrganizations: function () {
        return (this.props.app.target_companies) || (this.props.app.target_publicbodies);
    },
    toggleType: function (event) {
        var installType = this.state.installType;
        installType = event.target.value;
        this.setState({installType: installType});
    },
    renderInstallType: function () {
        var installTypeRestrictions = {personal: this.hasCitizens(), org: this.hasOrganizations()};
        var installType = this.getInstallType();

        var personal = !installTypeRestrictions.personal ? null
            : (<div className='radio col-sm-offset-3 col-sm-8'>
                    <label>
                        <input type="radio" value="PERSONAL" checked={installType == 'PERSONAL'}
                               onChange={this.toggleType}/>{this.context.t('install.org.type.PERSONAL')}
                    </label>
                </div>
            );


        var org = !installTypeRestrictions.org ? null
            : (<div className='radio col-sm-offset-3 col-sm-8'>
                    <label>
                        <input type="radio" value="ORG" checked={installType == 'ORG'}
                               onChange={this.toggleType}/>{this.context.t('install.org.type.ORG')}
                    </label>
                </div>
            );

        return (
            <div>
                <h4>{this.context.t('install.orgType.title')}</h4>
                <div className="form-group">
                    {personal}
                    {org}
                </div>
            </div>
        );
    },
    renderLabel: function (htmlFor, class_name, label, isRequired) {
        return (
            <label htmlFor={htmlFor} className={class_name}>{label} {isRequired ? '*' : ''} </label>
        );
    },
    changeInputContact: function (fieldname) {
        return function (event) {
            var org = this.state.installData;
            org.contact[fieldname] = event.target.value;
            this.setState({installData: org, errors: []});
        }.bind(this);
    },
    changeInputAddress: function (fieldname, value, isNumericField) {
        var org = Object.assign({address: {}}, this.state.installData);
        if (isNumericField && value !== '') {
            org.address[fieldname] = value.trim().search(/^\d+$/) !== -1 ? value.trim() : org.address[fieldname];
        } else {
            org.address[fieldname] = value;
        }
        this.setState({installData: org, errors: []});
    },
    validateContact: function () {
        var errs = [];
        var contact = this.state.installData.contact;
        if (!contact.contact_name || contact.contact_name.trim() == '') {
            errs.push('name');
        }
        if (!contact.contact_lastname || contact.contact_lastname.trim() == '') {
            errs.push('lastname');
        }

        this.setState({errors: errs});
        return errs.length <= 0;
    },
    validateAddress: function () {
        var errs = this.state.errors;
        var address = this.state.installData.address;
        if (!address.city || address.city.trim() == '') {
            errs.push('city');
        }
        if (!address.zip || address.zip.trim() == '') {
            errs.push('zip');
        }
        if (!address.country || address.country.trim() == '') {
            errs.push('country');
        }

        this.setState({errors: errs});
        return errs.length <= 0;
    },
    validateAndContinue: function () {
        var installType = this.getInstallType();
        if (installType === 'PERSONAL') {
            if ((this.validateContact() && this.validateAddress())) {
                this.props.continueInstallProcess();
            }
        } else if (installType === 'ORG' && this.validateContact() && (this.refs.setOrgComponent && this.refs.setOrgComponent.validate())) {
            this.props.continueInstallProcess();
        }
    },

    render: function () {
        var installType = this.getInstallType();

        return (
            <div className='form-horizontal'>
                {this.renderInstallType()}
                <h4>{this.context.t('search.contact.title')}</h4>
                <ContactSearchFormControl renderLabel={this.renderLabel} orgSearchData={this.state.installData.contact}
                                          changeInput={this.changeInputContact} errors={this.state.errors}/>

                {(installType === 'PERSONAL') || (this.props.app.type === "service")
                    ? (<div>
                        <h4>{this.context.t('search.contact.address.title')}</h4>
                        <AddressComponent ref='addressComponent' errors={this.state.errors}
                                          addressContainer={this.state.installData.address}
                                          changeInput={this.changeInputAddress}/>
                        <div className="next">
                            <button className="btn btn-default pull-right"
                                    onClick={this.validateAndContinue}>{this.context.t('ui.next')}</button>
                        </div>
                    </div>)
                    : (<div>
                        <h4>{this.context.t('search.organization.title')}</h4>
                        <SetOrganizationComponent ref='setOrgComponent' url={this.props.url} app={this.props.app}
                                                  orgs={this.props.orgs}
                                                  isOnlyForCitizens={this.isOnlyForCitizens}/>
                        <div className="next">
                            <button className="btn btn-default pull-right"
                                    onClick={this.validateAndContinue}>{this.context.t('ui.next')}</button>
                        </div>
                    </div>)
                }
            </div>
        );
    }
});
InstallForm.contextTypes = {
    t: PropTypes.func.isRequired
};

var ContactSearchFormControl = createClass({
    render: function () {
        var formDivClassName = this.props.error ? "form-group has-error" : "form-group";

        return (

            <div>
                <div className={formDivClassName}>
                    {this.props.renderLabel("contact-name", 'control-label col-sm-3 required', this.context.t('search.contact.name'), true)}
                    <div className="col-sm-8">
                        <input type="text" id="contact-name" className="form-control"
                               value={this.props.orgSearchData.contact_name}
                               onChange={this.props.changeInput('contact_name')} maxLength={100}
                               placeholder={this.context.t('search.contact.name')}/>
                    </div>
                </div>
                <div className="form-group">
                    {this.props.renderLabel("contact-lastname", 'control-label col-sm-3 required', this.context.t('search.contact.lastname'), true)}
                    <div className="col-sm-8">
                        <input type="text" id="contact-lastname" className="form-control"
                               value={this.props.orgSearchData.contact_lastname}
                               onChange={this.props.changeInput('contact_lastname')} maxLength={100}
                               placeholder={this.context.t('search.contact.lastname')}/>
                    </div>
                </div>
            </div>
        )
    }
});
ContactSearchFormControl.contextTypes = {
    t: PropTypes.func.isRequired
};

var OrganizationSearchFormControl = createClass({
    renderType: function () {
        var restriction = this.props.typeRestriction ? this.props.typeRestriction : {company: true, public_body: true};

        var public_body = null;
        if (restriction.public_body) {
            public_body = (
                <label className="radio-inline col-sm-3">
                    <input type="radio" value="PUBLIC_BODY"
                           checked={this.props.orgSearchData.sector_type == 'PUBLIC_BODY'}
                           onChange={this.props.toggleType}/>{this.context.t('search.organization.sector-type.PUBLIC_BODY')}
                </label>
            );
        }

        var company = null;
        if (restriction.company) {
            company = (
                <label className="radio-inline col-sm-3">
                    <input type="radio" value="COMPANY" checked={this.props.orgSearchData.sector_type == 'COMPANY'}
                           onChange={this.props.toggleType}/>{this.context.t('search.organization.sector-type.COMPANY')}
                </label>
            );
        }

        var formGroupClass = ($.inArray('sector_type', this.props.errors) != -1) ? 'form-group has-error' : 'form-group';

        return (
            <div className={formGroupClass}>
                <label htmlFor="organization-sector-type"
                       className="col-sm-3 control-label required">{this.context.t('search.organization.sector-type')} *</label>
                <div className="col-sm-8">
                    {public_body}
                    {company}
                </div>
            </div>
        );
    },

    render: function () {
        var label_regNum;
        switch (this.props.orgSearchData.country) {
            case 'България' :
                label_regNum = this.context.t('search.organization.business-id.bg');
                break;
            case 'Italia'   :
                label_regNum = this.context.t('search.organization.business-id.it');
                break;
            case 'France'   :
                label_regNum = this.context.t('search.organization.business-id.fr');
                break;
            case 'España'   :
                label_regNum = this.context.t('search.organization.business-id.es');
                break;
            case 'Türkiye'  :
                label_regNum = this.context.t('search.organization.business-id.tr');
                break;
            default         :
                label_regNum = this.context.t('search.organization.business-id.en');
                break;
        }
        if ((!this.props.orgSearchData.country_uri || this.props.orgSearchData.country_uri === "") && this.refs.orgCountrySelect) {
            this.props.orgSearchData.country_uri = this.refs.orgCountrySelect.getValue(this.props.orgSearchData.country);
        }

        return (
            <div>
                {this.renderType()}
                <div
                    className={($.inArray('country', this.props.errors) != -1) ? 'form-group has-error' : 'form-group'}>
                    {this.props.renderLabel("country", this.context.t('search.organization.country'), true)}
                    <div className="col-sm-8">
                        <CountrySelect ref="orgCountrySelect" className="form-control"
                                       url={'/api/store/dc-countries'} defLabel={this.props.orgSearchData.country}
                                       onChange={this.props.changeInput('country')}/>
                    </div>
                </div>
                <div
                    className={($.inArray('legal_name', this.props.errors) != -1) ? 'form-group has-error' : 'form-group'}>
                    {this.props.renderLabel("legal_name", this.context.t('search.organization.legal-name'), true)}
                    <div className="col-sm-8">
                        <input type="text" id="legal_name" className="form-control"
                               value={this.props.orgSearchData.legal_name}
                               onChange={this.props.changeInput('legal_name')} maxLength={100}
                               placeholder={this.context.t('search.organization.legal-name')}/></div>
                </div>
                <div
                    className={($.inArray('tax_reg_num', this.props.errors) != -1) ? 'form-group has-error' : 'form-group'}>
                    {this.props.renderLabel("tax_reg_num", label_regNum, true)}
                    <div className="col-sm-8">
                        <input type="text" id="tax_reg_num" className="form-control"
                               value={this.props.orgSearchData.tax_reg_num}
                               onChange={this.props.changeInput('tax_reg_num')} maxLength={20}
                               placeholder={this.context.t(label_regNum)}/></div>
                </div>
            </div>
        )
    }
});
OrganizationSearchFormControl.contextTypes = {
    t: PropTypes.func.isRequired
};

var AddressComponent = createClass({
    changeInput: function (fieldname, isNumericField) {
        var changeInput = this.props.changeInput;
        return function (event) {
            if (event.added) {
                changeInput(fieldname, event.added.name, isNumericField);
                changeInput(fieldname + "_uri", event.target.value, isNumericField);
            } else if (fieldname === "country") {
                changeInput(fieldname, event.target.selectedOptions[0].label, isNumericField);
                changeInput(fieldname + "_uri", event.target.value, isNumericField);
                //If country has changed, the city is not longer valid
                changeInput("city", "", isNumericField);
                changeInput("city_uri", "", isNumericField);
                if (this.refs.geoSearchCity) {
                    this.refs.geoSearchCity.clear();
                } //works only to remove tags in current component state, geoSelect placeholder still there.
            } else {
                changeInput(fieldname, event.target.value, isNumericField);
            }
        }.bind(this)
    },
    handleCitySelected: function (city) {
        this.props.changeInput('city_uri', city.uri, false);
        this.props.changeInput('city', city.name, false);
    },
    handleCityChange: function (e) {
        const el = e.currentTarget;
        this.props.changeInput('city', el.value, false);
    },
    render: function () {
        var address = this.props.addressContainer;
        var addressType = this.props.addressType ? this.props.addressType : '';

        return (
            <div>

                <Field name="street_and_number" error={$.inArray("street_and_number", this.props.errors) != -1}
                       isRequired={(addressType == 'ORG')}>
                    <input className="form-control" id="street_and_number" type="text" value={address.street_and_number}
                           onChange={this.changeInput('street_and_number')}/>
                </Field>
                {(addressType !== 'ORG') ? '' : //personal address
                    <Field name="po_box">
                        <input className="form-control" id="po_box" type="text" value={address.po_box}
                               onChange={this.changeInput('po_box')}/>
                    </Field>
                }
                <Field name="country" class_name_div='col-sm-5' error={$.inArray("country", this.props.errors) != -1}
                       isRequired={true}>
                    <CountrySelect className="form-control" url={"/api/store/dc-countries"} value={address.country_uri}
                                   defLabel={address.country} onChange={this.changeInput('country')}
                                   disabled={this.props.disabled}/>
                </Field>
                <Field name="city" error={$.inArray("city", this.props.errors) != -1} isRequired={true}>
                    <GeoAreaAutosuggest name="city"
                                        countryUri={address.country_uri}
                                        endpoint="/dc-cities"
                                        onChange={this.handleCityChange}
                                        onGeoAreaSelected={this.handleCitySelected}
                                        value={address.city}/>
                </Field>
                <Field name="zip" class_name_div='col-sm-3' error={$.inArray("zip", this.props.errors) != -1}
                       isRequired={true}>
                    <input className="form-control" id="zip" type="text" maxLength={6} value={address.zip}
                           onChange={this.changeInput('zip', true)}/>
                </Field>
                {(addressType !== 'ORG') ? '' : //personal address
                    <Field name="cedex" class_name_div='col-sm-2'>
                        <input className="form-control" id="cedex" type="cedex" maxLength={3} value={address.cedex}
                               onChange={this.changeInput('cedex', true)}/>
                    </Field>
                }
            </div>
        );
    }
});
AddressComponent.contextTypes = {
    t: PropTypes.func.isRequired
};

/* PROPS: name, className, class_name_div, error, isRequired, (children)*/
var Field = createClass({
    renderLabel: function (htmlFor, class_name, label, isRequired) {
        var cn = isRequired ? class_name + ' required' : class_name;
        return (
            <label htmlFor={htmlFor} className={cn}>{label} {isRequired ? '*' : ''}</label>
        );
    },
    render: function () {
        var className = "control-label col-sm-3";
        var classNameDiv = "col-sm-7";
        if (this.props.class_name) {
            className = this.props.class_name;
        }
        if (this.props.error) {
            className = className + " error";
        }
        if (this.props.class_name_div) {
            classNameDiv = this.props.class_name_div;
        }
        return (
            <div className="form-group">
                {this.renderLabel(this.props.name, className, this.context.t('my.network.organization.' + this.props.name), this.props.isRequired)}
                <div className={classNameDiv}>
                    {this.props.children}
                </div>
            </div>
        );
    }
});
Field.contextTypes = {
    t: PropTypes.func.isRequired
};

//http://stackoverflow.com/questions/25793918/creating-select-elements-in-react-js
/** PROPS: onChange(), url */
var CountrySelect = createClass({
    propTypes: {url: PropTypes.string.isRequired},
    getInitialState: function () {
        return {options: [], countries: []}
    },
    onChange: function (event) {
        this.props.onChange(event);
    },
    componentDidMount: function () {
        //var userCurrentLanguge = currentLanguage;
        if (this.props.url) {
            // get country dc data
            $.ajax({
                url: this.props.url,
                type: 'get',
                dataType: 'json',
                data: {q: ' '},
                success: function (data) {
                    var areas = data.areas;
                    var options = [{value: '', label: ''}];
                    areas = areas.filter(function (n) {
                        return n !== null;
                    });
                    for (var i = 0; i < areas.length; i++) {
                        options.push({value: areas[i].uri, label: areas[i].name})
                    }
                    this.state.countries = options;
                    this.successHandler(options); //set the list of countries
                }.bind(this),
                error: function (xhr, status, err) {
                    console.error(status, err.toString());
                }.bind(this)
            });
        }
    },
    successHandler: function (data) {
        // assuming data is an array of {name: "foo", value: "bar"}
        for (var i = 0; i < data.length; i++) {
            var option = data[i];
            this.state.options.push(<option key={i} value={option.value}>{option.label}</option>);
        }
        this.setState(this.state);
        this.forceUpdate();
    },
    getValue: function (label) {
        if (!label || label !== "") {
            for (var i = 0; i < this.state.countries.length; i++) {
                if (this.state.countries[i].label === label) {
                    return this.state.countries[i].value;
                }
            }
        }
        return null;
    },

    render: function () {
        /*
        var label = this.props.defLabel;
        if(label && (!this.props.value || this.props.value === "") ){
            //This is to load the country_uri that couldn't be set |
            this.props.value = (this.getValue(label)); // decodeURIComponent()
        }*/
        const value = (!this.props.value || this.props.value === "") ? this.getValue(this.props.defLabel) : this.props.value;

        // the parameter "value=" is selected option. Default selected option can either be set here. Using browser-base fonctuion decodeURIComponent()
        return (
            <select className="form-control" id="country" onChange={this.onChange}
                    value={value || ''} disabled={this.props.disabled}>
                {this.state.options}
            </select>
        );
    }
});

/** PROPS: app{}, orgs[], url, isOnlyForCitizens() */
var SetOrganizationComponent = createClass({
    getInitialState: function () {
        return {
            orgSearchData: {
                contact_name: '', contact_lastname: '',
                typeInstallOrg: 'NEW-ORGS',
                sector_type: '', country: 'France', legal_name: '', tax_reg_num: '',
                selectedOrgId: ''
            },
            errors: []
        }
    },
    getOrgSearchData: function () {
        return this.state.orgSearchData
    },
    orgTypeRestriction: function () {
        return {
            company: this.props.app.target_companies,
            public_body: this.props.app.target_publicbodies
        };
    },
    onChangeOrgInput: function (fieldname) {
        return function (event) {
            var org = this.state.orgSearchData;
            if (fieldname === "country") {
                org[fieldname + "_uri"] = event.target.value;
                org[fieldname] = event.target.selectedOptions[0].label;
            } else if (fieldname === "tax_reg_num") {
                org[fieldname] = '' + event.target.value.replace(/\s+/g, '');
                /*Remove whitespace avoiding setting undefined*/
            } else {
                org[fieldname] = event.target.value;
            }
            this.setState({orgSearchData: org, errors: []});
        }.bind(this);
    },
    toggleInstallOrgType: function (event) {
        var org = this.state.orgSearchData;
        org.typeInstallOrg = event.target.value;
        this.setState({orgSearchData: org, errors: []});
    },
    toggleSectorType: function (event) {
        var org = this.state.orgSearchData;
        org.sector_type = event.target.value;
        this.setState({orgSearchData: org, errors: []});
    },
    renderOrganizations: function () {
        var opts = [];
        opts.push(<option key={-1} value=""></option>);
        this.props.orgs.map(function (org) {
            opts.push(<option key={org.id} value={org.id}>{org.name}</option>);
        });

        var formGroupClass = ($.inArray('typeInstallOrg', this.state.errors) != -1) ? 'form-group has-error' : 'form-group';

        return (
            <div className={formGroupClass}>
                {this.renderLabel('organization', this.context.t('search.organization.title'), true)}
                <div className="col-sm-8">
                    <select id="organization" className="col-sm-8 form-control"
                            onChange={this.onChangeOrgInput('selectedOrgId')}
                            value={this.state.orgSearchData.selectedOrgId || ''}>
                        {opts}
                    </select>
                </div>
            </div>
        );
    },
    validate: function () {
        var state = this.state;
        var errors = [];
        var orgSearchData = state.orgSearchData;
        if (orgSearchData.typeInstallOrg === 'EXISTING-ORGS') {
            if (orgSearchData.selectedOrgId.trim() === '') {
                errors.push('typeInstallOrg');
            }
        } else {
            if (!orgSearchData.sector_type || orgSearchData.sector_type.trim() == '') {
                errors.push('sector_type')
            }
            if (!orgSearchData.country_uri || orgSearchData.country_uri.trim() == '') {
                errors.push('country')
            }
            if (!orgSearchData.legal_name || orgSearchData.legal_name.trim() == '') {
                errors.push('legal_name')
            }
            if (!orgSearchData.tax_reg_num || orgSearchData.tax_reg_num.trim() == '') {
                errors.push('tax_reg_num')
            }
        }

        state.errors = errors;
        this.setState(state);
        return state.errors.length <= 0;
    },
    renderLabel: function (htmlFor, label, isRequired) {
        var labelClass = isRequired ? 'col-sm-3 control-label required' : 'col-sm-3 control-label';
        return (
            <label htmlFor={htmlFor} className={labelClass}>{label} {isRequired ? '*' : ''}</label>
        );
    },
    render: function () {
        return (
            <div>
                <div className="form-group">
                    <div className="col-sm-offset-3 col-sm-8">

                        <div className="radio">
                            <label>
                                <input type="radio" value="NEW-ORGS"
                                       checked={this.state.orgSearchData.typeInstallOrg == 'NEW-ORGS'}
                                       onChange={this.toggleInstallOrgType}/>{this.context.t('search.organization.selection.new')}
                            </label>
                        </div>
                        {(!this.props.isOnlyForCitizens() && this.props.orgs && this.props.orgs.length > 0)
                            ?
                            <div className="radio">
                                <label>
                                    <input type="radio" value="EXISTING-ORGS"
                                           checked={this.state.orgSearchData.typeInstallOrg == 'EXISTING-ORGS'}
                                           onChange={this.toggleInstallOrgType}/>{this.context.t('search.organization.selection.existing')}
                                </label>
                            </div>
                            : ''
                        }
                    </div>
                </div>

                {this.state.orgSearchData.typeInstallOrg === 'EXISTING-ORGS' ?
                    this.renderOrganizations() : ''}

                {this.state.orgSearchData.typeInstallOrg === 'NEW-ORGS' ?
                    <OrganizationSearchFormControl errors={this.state.errors}
                                                   renderLabel={this.renderLabel}
                                                   typeRestriction={this.orgTypeRestriction()}
                                                   orgSearchData={this.state.orgSearchData}
                                                   changeInput={this.onChangeOrgInput}
                                                   toggleType={this.toggleSectorType}/> : ''}
            </div>
        );
    }
});
SetOrganizationComponent.contextTypes = {
    t: PropTypes.func.isRequired
};
//END NEW INSTALL PROCESS

/** PROPS: images */
var Carousel = createClass({
    getInitialState: function () {
        return {index: 0};
    },
    back: function () {
        var index = this.state.index;
        index = Math.max(0, index - 1);
        this.setState({index: index});
    },
    forward: function () {
        var index = this.state.index;
        index = Math.min(this.props.images.length, index + 1);
        this.setState({index: index});
    },

    render: function () {
        if (!this.props.images) {
            return null;
        }

        var back = null;
        if (this.state.index > 0) {
            back = <a className="back" onClick={this.back}>
                <i className="fa fa-chevron-left"></i>
            </a>;
        }

        var forward = null;
        if (this.state.index < this.props.images.length - 1) {
            forward = <a className="forward" onClick={this.forward}>
                <i className="fa fa-chevron-right"></i>
            </a>;
        }

        return (
            <div className="col-md-12">
                <div className="app-screenshots">
                    {back}
                    <img src={this.props.images[this.state.index]} alt={this.state.index}/>
                    {forward}
                </div>
            </div>
        );
    }
});

export default connect(state => {
    return {
        config: state.config,
        userInfo: state.userInfo
    }
}, null, null, {withRef: true})(AppModal);
