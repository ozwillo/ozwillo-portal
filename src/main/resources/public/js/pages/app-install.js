import React from 'react';
import Slider from 'react-slick';
import ModalImage from '../components/modal-image';
import {
    buyApplication,
    fetchAppDetails,
    fetchAvailableOrganizations,
    fetchRateApp,
    unavailableOrganizationToInstallAnApp
} from '../util/store-service';
import RatingWrapper from '../components/rating';
import Select from 'react-select';
import PropTypes from 'prop-types';
import {Link} from 'react-router-dom';
import OrganizationService from '../util/organization-service';
import {i18n} from '../config/i18n-config';
import {t, Trans} from '@lingui/macro'
import UpdateTitle from '../components/update-title';
import Spinner from '../components/spinner';
import UserService from '../util/user-service';


const Showdown = require('showdown');
const converter = new Showdown.Converter({tables: true});

export default class AppInstall extends React.Component {

    state = {
        app: {},
        appDetails: {
            longdescription: '',
            policy: '',
            rateable: false,
            rating: 0,
            screenshots: [],
            serviceUrl: null,
            tos: ''
        },
        config: {},
        organizationsAvailable: {organizations: [], loading: true},
        user: {}
    };

    constructor(props) {
        super(props);
        this._organizationService = new OrganizationService();
        this._userService = new UserService();
    }

    componentDidMount = async () => {
        const {app, config} = this.props.location.state;
        const user = await this._userService.fetchUserInfos();
        this.setState({app: app, config: config, user: user}, async () => {
            await this._loadAppDetails();
            if(user) {
                await this._loadOrgs();
            }
        });
    };

    _loadOrgs = async () => {
        const {app} = this.state;
        const data = await fetchAvailableOrganizations(app.type, app.id);
        this.setState({organizationsAvailable: {organizations: data, loading: false}});

        const newOrganizationsAvailable = await this._disableOrganizationWhereAppAlreadyInstalled(data);
        this.setState({organizationsAvailable: {organizations: newOrganizationsAvailable, loading: false}});
    };

    _disableOrganizationWhereAppAlreadyInstalled = async (allOrganizations) => {
        const {app} = this.state;
        const organizationUnavailable = await unavailableOrganizationToInstallAnApp(app.id, app.type)
        allOrganizations.forEach(org => {
            organizationUnavailable.forEach(unAvailableOrg => {
                if(org.id === unAvailableOrg.id){
                    org.disabled = true;
                }
            })
        });
        return allOrganizations;
    };

    _loadAppDetails = async () => {
        const {app} = this.state;
        const data = await fetchAppDetails(app.type, app.id);
        this.setState({appDetails: data});
    };

    _rateApp = async (rate) => {
        let {app, appDetails} = this.state;
        await fetchRateApp(app.type, app.id, rate);
        appDetails.rateable = false;
        appDetails.rating = rate;
        this.setState({appDetails});
    };

    _displayScreenShots = (arrayScreenshots) => {
        if (arrayScreenshots) {
            return arrayScreenshots.map((screenshot, index) => {
                return (
                    <div key={index} onClick={() => this._openModal(screenshot)}>
                        <img className={'screenshot'} src={screenshot} alt={'screenshot' + index}/>
                    </div>
                )
            })
        }
    };

    _openModal = (imageSrc) => {
        this.refs.modalImage._openModal(imageSrc);
    };

    scrollToLongDescription = () => { // run this method to execute scrolling.
        window.scrollTo({
            top: this.longDescription.offsetTop,
            behavior: 'smooth'  // Optional, adds animation
        })
    };


    render() {
        const {app, appDetails, organizationsAvailable, config, user} = this.state;
        const settings = {
            dots: true,
            speed: 500,
            slidesToScroll: 1,
            variableWidth: true,
            accessibility: true,
        };

        return (
            <div className={'app-install-wrapper'}>
                <UpdateTitle title={app.name}/>
                <div className={'flex-row header-app-install'}>
                    <div className={'information-app flex-row'}>
                        <img alt={'app icon'} src={app.icon}/>
                        <div className={'information-app-details'}>
                            <p><strong>{app.name}</strong></p>
                            <p>{app.provider}</p>
                            <p>{app.description}
                                &nbsp;
                                {
                                    appDetails.longdescription === app.description ?
                                        null :
                                        <i className="fas fa-external-link-alt go-to-long-description"
                                           onClick={this.scrollToLongDescription}/>
                                }
                            </p>
                            <div className={'rate-content'}>
                                <RatingWrapper rating={appDetails.rating} rateable={appDetails.rateable}
                                               rate={this._rateApp}/>
                            </div>
                        </div>
                    </div>
                    <div className={'install-app'}>
                        <div className={'dropdown'}>
                            <InstallForm user={user} app={app} organizationsAvailable={organizationsAvailable}
                                         config={config}/>
                        </div>

                    </div>
                </div>


                {
                    appDetails.screenshots && appDetails.screenshots.length > 1 &&
                    <div className={'app-install-carousel'}>
                        <div className={'carousel-container'}>
                            <Slider {...settings}>
                                {this._displayScreenShots(appDetails.screenshots)}
                            </Slider>
                        </div>
                    </div>
                }

                {
                    appDetails.screenshots && appDetails.screenshots.length === 1 &&
                    <div className={'unique-screenshot'}>
                        <img src={appDetails.screenshots[0]}
                             onClick={() => this._openModal(appDetails.screenshots[0])}
                             alt={'screenshot ' + appDetails.screenshots[0]}/>
                    </div>

                }

                <div className={'flex-row app-install-description'} ref={(ref) => this.longDescription = ref}>
                    {
                        appDetails.longdescription === app.description ?
                            <div dangerouslySetInnerHTML={{__html: converter.makeHtml(app.description)}}/>
                            :
                            <div className={'long'}
                                 dangerouslySetInnerHTML={{__html: converter.makeHtml(appDetails.longdescription)}}/>
                    }
                </div>
                < ModalImage
                    ref={'modalImage'}
                />
            </div>
        )
    }
}


export class InstallForm extends React.Component {

    state = {
        installType: null,
        error: {status: false, http_status: 200},
        organizationSelected: null,
        buying: false,
        installed: false
    };

    _hasCitizens = () => {
        return this.props.app.target_citizens;
    };

    _hasOrganizations = () => {
        return (this.props.app.target_companies) || (this.props.app.target_publicbodies);
    };

    _createOptions = () => {
        let options = [];
        this._hasCitizens() ? options.push({
            value: i18n._('install.org.type.PERSONAL'),
            label: 'PERSONAL',
            id: 1
        }) : null;

        this._hasOrganizations() ? options.push({
            value: i18n._('install.org.type.ORG'),
            label: 'ORG',
            id: 2
        }) : null;

        return options;
    };

    _installButtonIsDisabled = () => {
        const {app} = this.props;
        const {installType, organizationSelected, buying} = this.state;
        if (!installType || buying || (organizationSelected && organizationSelected.disabled)) {
            return true;
        } else if (installType && !(installType === 'PERSONAL') && !(app.type === 'service') && organizationSelected) {
            return false;
        } else if (installType && ((installType === 'PERSONAL') || (app.type === 'service'))) {
            return false;
        } else {
            return true;
        }
    };

    _doInstallApp = async () => {
        const {app} = this.props;
        const {organizationSelected} = this.state;
        // set buying to true to display the spinner until any below ajax response is received.
        this.setState({installed: false, buying: true, error: {status: false, http_status: 200}});
        try {
            await buyApplication(app.id, app.type, organizationSelected);
            this.setState({buying: false, installed: true});
        } catch (error) {
            this.setState({buying: false, error: {status: true, http_status: error.status}})
        }
    };


    render() {
        const options = this._createOptions();
        const {installType, organizationSelected, buying, installed, error} = this.state;
        const {organizationsAvailable, app, user} = this.props;
        let disabledOrganization = !installType;

        if (!user) {
            return (
                <div className={'flex-row install-area redirect-to-connection'}>
                    <div className="install">
                        <button className="btn pull-right btn-install">{i18n._('store.install')}</button>
                    </div>

                </div>
            )
        }


        return (
            <React.Fragment>
                {!installed &&
                <div className={'install-selector'}>
                    <label><Trans>For which usage</Trans></label>
                    <Select
                        className="select"
                        value={installType}
                        labelKey="value"
                        valueKey="id"
                        onChange={(value) => this.setState({installType: value})}
                        clearable={false}
                        options={options}/>
                </div>
                }

                {!installed && !(installType === 'PERSONAL') && !(app.type === 'service') ?
                    <div className={'install-selector'}>
                        <label className={organizationsAvailable.loading ? 'label-spinner' : null}><Trans>For which organization</Trans></label>
                        <div className={'select-organization'}>
                            <Spinner display={organizationsAvailable.loading} className={'organization-spinner'}/>
                            <Select
                                disabled={disabledOrganization || organizationsAvailable.loading}
                                className={'select'}
                                value={organizationSelected}
                                labelKey="name"
                                valueKey="id"
                                onChange={(value) => this.setState({organizationSelected: value})}
                                clearable={false}
                                options={organizationsAvailable.organizations}
                            />
                        </div>
                    </div>
                    : null
                }

                <div className={'flex-row install-area'}>
                    <Spinner display={buying}/>

                    {installed ?
                        <div className="install installed">
                            <Link className="btn btn-default-inverse pull-right btn-install"
                                  to={`/${this.props.config.language}/store`}>
                                {i18n._('ui.appstore')}
                            </Link>
                            <Link className="btn btn-default-inverse pull-right btn-install dashboard"
                                  to={'/my/dashboard'}>
                                {i18n._('my.dashboard')}
                            </Link>
                        </div>
                        :
                        <div className="install">
                            <button className="btn pull-right btn-install"
                                    disabled={this._installButtonIsDisabled()}
                                    onClick={this._doInstallApp}>{i18n._('store.install')}</button>
                        </div>
                    }
                </div>
                {error.http_status !== 200 &&
                <div className={'alert alert-danger'}>
                    <p>{i18n._('could-not-install-app')}</p>
                </div>
                }
            </React.Fragment>
        )
    }

}


InstallForm.propTypes = {
    app: PropTypes.object.isRequired,
    organizationsAvailable: PropTypes.object.isRequired,
    user: PropTypes.object
};


