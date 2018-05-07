'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import {connect} from 'react-redux';
import createClass from 'create-react-class';
import PropTypes from 'prop-types'
import { withRouter } from 'react-router-dom';

const Showdown = require('showdown');
const converter = new Showdown.Converter({tables: true});

import {Modal} from './bootstrap-react';
import {RatingWrapper} from './rating';

class AppModal extends React.Component {

    static contextTypes = {
        t: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = this.initialState;

        this.loadApp = this.loadApp.bind(this);
        this.loadOrgs = this.loadOrgs.bind(this);
        this.open = this.open.bind(this);
        this.close = this.close.bind(this);
        this.doInstallApp = this.doInstallApp.bind(this);
        this.rateApp = this.rateApp.bind(this);
        this.continueInstallProcess = this.continueInstallProcess.bind(this);
        this.renderBuying = this.renderBuying.bind(this);
        this.renderInstallingForm = this.renderInstallingForm.bind(this);
        this.renderAppDescription = this.renderAppDescription.bind(this);
        this.renderSucessfulInstallationForm = this.renderSucessfulInstallationForm.bind(this);
    }

    get initialState () {
        return {
            app: {rating: 0, rateable: true, tos: '', policy: '', longdescription: '', screenshots: null},
            orgs: [],
            selectedOrg: '',
            buying: false,
            installing: false,
            isInstalled: false,
            error: false
        };
    }

    componentDidUpdate() {
        const desc = $(ReactDOM.findDOMNode(this)).find(".app-description table");
        desc.addClass("table table-bordered table-condensed table-striped");
    }

    loadApp() {
        $.ajax({
            url: "/api/store/details/" + this.props.app.type + "/" + this.props.app.id,
            type: 'get',
            dataType: 'json',
            success: (data) => {
                this.setState({ app: data });
            },
            error: (xhr, status, err) => {
                console.error(status, err.toString());
            }
        });
    }

    loadOrgs() {
        $.ajax({
            url: "/api/store/organizations/" + this.props.app.type + "/" + this.props.app.id,
            type: 'get',
            dataType: 'json',
            success: (data) => {
                this.setState({ orgs: data });
            },
            error: (xhr, status, err) => {
                console.error(status, err.toString());
            }
        });
    }

    open() {
        this.setState(this.initialState);

        const href = `/${this.props.config.language}/store/${this.props.app.type}/${this.props.app.id}`;
        this.props.history.replace(href);

        this.loadApp();
        const isLogged = !!this.props.userInfo.sub;
        if (isLogged) {
            this.loadOrgs();
        }

        this.refs.modal.open();
    }

    close () {
        this.props.history.replace(`/${this.props.config.language}/store`);

        if (this.state.isInstalled) {
            this.props.history.push('/my/dashboard');
        }
    }

    doInstallApp(organizationId) {

        // set buying to true to display the spinner until any below ajax response is received.
        this.setState({ installing: false, buying: true });

        let request = {appId: this.props.app.id, appType: this.props.app.type};
        if (organizationId) {
            request.organizationId = organizationId;
        }

        $.ajax({
            url: '/api/store/buy',
            type: 'post',
            data: JSON.stringify(request),
            contentType: 'application/json',
            success: (data) => {
                if (data.success) {
                    this.setState({ buying: false, isInstalled: true });
                } else {
                    this.setState({ buying: false, error: true });
                }
            },
            error: (xhr, status, err) => {
                console.error(status, err.toString());
                this.setState({ buying: false, error: true });
            }
        });
    }

    rateApp(rate) {
        $.ajax({
            url: "/api/store/rate/" + this.props.app.type + "/" + this.props.app.id,
            type: 'post',
            contentType: 'application/json',
            data: JSON.stringify({rate: rate}),
            success: () => {
                const state = this.state;
                state.app.rateable = false;
                state.app.rating = rate;
                this.setState(state);
            },
            error: (xhr, status, err) => {
                console.error(status, err.toString());
            }
        });
    }

    continueInstallProcess(installType, selectedOrgId) {
        const state = this.state;
        state.installType = installType;

        if (state.installType === 'ORG') {
            state.installing = false;
            this.doInstallApp(selectedOrgId);
        } else { // PERSONAL
            this.doInstallApp(null);
        }
    }

    renderBuying() {
        return (<h3><i className="fa fa-spinner fa-spin loading"/> {this.context.t('buying')}</h3>);
    }

    renderInstallingForm() {
        return (<InstallForm url={this.state.app.serviceUrl}
                             app={this.props.app}
                             orgs={this.state.orgs}
                             continueInstallProcess={this.continueInstallProcess}
        />);
    }

    renderAppDescription() {
        return (
            <AppDescriptionComponentWithRedux app={this.props.app} stateApp={this.state.app}
                                              rateApp={this.rateApp}
                                              onInstallButton={() => this.setState({ installing: true })}
                                              error={this.state.error}/>
        );
    }

    renderSucessfulInstallationForm() {
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
    }

    render() {
        let title = this.props.app.name;
        let content = null;
        if (this.state.buying) {
            content = this.renderBuying();
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
}



/** PROPS: app{}, stateApp{}, rateApp(), onInstallButton(), errors[] */
const AppDescriptionComponent = createClass({
    render: function () {
        const stateApp = this.props.stateApp;

        const carousel = (stateApp.screenshots && stateApp.screenshots.length > 0)
            ? (<div className="row">
                <Carousel images={stateApp.screenshots}/>
            </div>)
            : null;

        const error = (this.props.error)
            ? (<div className="alert alert-danger alert-dismissible" role="alert">
                <button type="button" className="close" data-dismiss="alert">
                    <span aria-hidden="true">&times;</span>
                    <span className="sr-only">{this.context.t('ui.close')}</span>
                </button>
                <strong>{this.context.t('sorry')}</strong> {this.context.t('could-not-install-app')}
            </div>)
            : null;

        let rateInfo = null;
        const isLogged = !!this.props.userInfo.sub;
        if (isLogged && !stateApp.rateable) {
            rateInfo = (<p>{this.context.t('already-rated')}</p>);
        }

        const description = converter.makeHtml(stateApp.longdescription);

        let launchOrInstallButton;

        if (this.props.app.type === "service" && this.props.app.installed) {
            if (this.props.stateApp && this.props.stateApp.serviceUrl) {
                launchOrInstallButton =
                    <a className="btn btn-default btn-lg pull-right" href={this.props.stateApp.serviceUrl}
                       target="_new">{this.context.t('launch')}</a>;
            } else {
                launchOrInstallButton = (<label> <i className="fa fa-spinner fa-spin loading"/> </label>);
            }
        } else {
            const storeUrl = `/${this.props.config.language}/store`;
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
const InstallForm = createClass({
    getInitialState: function () {
        return ({
            installType: 'PERSONAL',
            errors: []
        })
    },
    getInstallType: function () {
        const installTypeRestrictions = {personal: this.hasCitizens(), org: this.hasOrganizations()};
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
        const installType = event.target.value;
        this.setState({installType: installType});
    },
    renderInstallType: function () {
        const installTypeRestrictions = {personal: this.hasCitizens(), org: this.hasOrganizations()};
        const installType = this.getInstallType();

        const personal = !installTypeRestrictions.personal ? null
            : (<div className='radio col-sm-offset-3 col-sm-8'>
                    <label>
                        <input type="radio" value="PERSONAL" checked={installType === 'PERSONAL'}
                               onChange={this.toggleType}/>{this.context.t('install.org.type.PERSONAL')}
                    </label>
                </div>
            );


        const org = !installTypeRestrictions.org ? null
            : (<div className='radio col-sm-offset-3 col-sm-8'>
                    <label>
                        <input type="radio" value="ORG" checked={installType === 'ORG'}
                               onChange={this.toggleType}/>{this.context.t('install.org.type.ORG')}
                    </label>
                </div>
            );

        return (
            <div>
                <h4>{this.context.t('choose-install-type')}</h4>
                <div className="form-group">
                    {personal}
                    {org}
                </div>
            </div>
        );
    },

    validateAndContinue: function () {
        const installType = this.getInstallType();
        if (installType === 'PERSONAL') {
            this.props.continueInstallProcess(installType, null);
        } else if (installType === 'ORG' && (this.refs.setOrgComponent && this.refs.setOrgComponent.validate())) {
            this.props.continueInstallProcess(installType, this.refs.setOrgComponent.getSelectedOrganization());
        }
    },

    render: function () {
        const installType = this.getInstallType();

        return (
            <div className='form-horizontal'>
                {this.renderInstallType()}

                {(installType === 'PERSONAL') || (this.props.app.type === "service")
                    ? (<div className="next">
                            <button className="btn btn-default pull-right"
                                    onClick={this.validateAndContinue}>{this.context.t('ui.next')}</button>
                        </div>
                    )
                    : (<div>
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

/** PROPS: app{}, orgs[], url, isOnlyForCitizens() */
const SetOrganizationComponent = createClass({
    getInitialState: function () {
        return {
            selectedOrgId: '',
            errors: []
        }
    },
    getSelectedOrganization: function () {
        return this.state.selectedOrgId
    },
    renderOrganizations: function () {
        const opts = [];
        opts.push(<option key={-1} value=""></option>);
        this.props.orgs.map(function (org) {
            opts.push(<option key={org.id} value={org.id}>{org.name}</option>);
        });

        const formGroupClass = ($.inArray('typeInstallOrg', this.state.errors) != -1) ? 'form-group has-error' : 'form-group';

        return (
            <div className={formGroupClass}>
                {this.renderLabel('organization', this.context.t('search.organization.title'), true)}
                <div className="col-sm-8">
                    <select id="organization" className="col-sm-8 form-control"
                            onChange={(event) => this.setState({ selectedOrgId: event.target.value })}
                            value={this.state.selectedOrgId || ''}>
                        {opts}
                    </select>
                </div>
            </div>
        );
    },
    validate: function () {
        const errors = [];
        if (this.state.selectedOrgId.trim() === '') {
            errors.push('typeInstallOrg');
        }

        this.setState({ errors: errors });
        return errors.length <= 0;
    },
    renderLabel: function (htmlFor, label, isRequired) {
        const labelClass = isRequired ? 'col-sm-3 control-label required' : 'col-sm-3 control-label';
        return (
            <label htmlFor={htmlFor} className={labelClass}>{label} {isRequired ? '*' : ''}</label>
        );
    },
    render: function () {
        return (
            <div>
                {this.renderOrganizations()}
            </div>
        );
    }
});
SetOrganizationComponent.contextTypes = {
    t: PropTypes.func.isRequired
};
//END NEW INSTALL PROCESS

/** PROPS: images */
const Carousel = createClass({
    getInitialState: function () {
        return {index: 0};
    },
    back: function () {
        let index = this.state.index;
        index = Math.max(0, index - 1);
        this.setState({index: index});
    },
    forward: function () {
        let index = this.state.index;
        index = Math.min(this.props.images.length, index + 1);
        this.setState({index: index});
    },

    render: function () {
        if (!this.props.images) {
            return null;
        }

        let back = null;
        if (this.state.index > 0) {
            back = <a className="back" onClick={this.back}>
                <i className="fa fa-chevron-left"></i>
            </a>;
        }

        let forward = null;
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

export default withRouter(connect(state => {
    return {
        config: state.config,
        userInfo: state.userInfo
    }
}, null, null, {withRef: true})(AppModal));
