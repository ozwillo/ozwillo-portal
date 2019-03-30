import React from 'react';
import PropTypes from 'prop-types';
import Select from 'react-select';
import { i18n } from "../../../config/i18n-config"
import NotificationMessageBlock from '../../notification-message-block';
import customFetch from '../../../util/custom-fetch';
import Config from '../../../config/config';

const AppTypes = Config.appTypes;
const MIN_APPS_NEEDED_TO_SCROLL = 7;

class AddInstanceDropdownHeader extends React.Component {

    static propTypes = {
        app: PropTypes.object,
        onAddInstance: PropTypes.func.isRequired,
        onChangeInstance: PropTypes.func.isRequired,
        organization: PropTypes.object
    };

    constructor(props) {
        super(props);

        this.state = {
            error: {message: '', http_status: 200},
            isLoading: false,
            apps: [],
            maybeMoreApps: false,
            totalAppsFetched: 0
        };

        //bind methods
        this.onOptionChange = this.onOptionChange.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
    }

    componentDidMount = async () =>  {
        await this._handleApplications();
    }

    onOptionChange(selectedOption) {
        this.props.onChangeInstance(selectedOption)
    }

    onSubmit(e) {
        e.preventDefault();

        this.setState({error: '',isLoading: true});

        this.props.onAddInstance()
            .then(() => this.setState({error: '', isLoading: false}))
            .catch(err => {
                console.error(err);
                let message = '';
                if(err.status.toString().startsWith('4')) {
                    message = i18n._('could-not-install-app-400')
                }else if(err.status.toString().startsWith('5')){
                    message = i18n._('could-not-install-app-500')
                }else{
                    message = i18n._('ui.error')
                }

                this.setState({error: {message: message, http_status: err.status}, isLoading: false});
            });
    }

    _fetchApplications = async () => {
        const {totalAppsFetched} = this.state;
        const {organization} = this.props;

        return await customFetch(`/api/store/applications`, {urlParams: {
                target_citizens: !organization.type,
                target_publicbodies: organization.type === 'PUBLIC_BODY',
                target_companies: organization.type === 'COMPANY',
                free: true,
                paid: true,
                appType: AppTypes.application,
                last: totalAppsFetched
            }});
    };

    _handleApplications = async () => {
        const {apps, maybeMoreApps, totalAppsFetched} = this.state;
        const {apps: appsFetched, maybeMoreApps: maybeMoreAppsFetched}  = await this._fetchApplications();

        let appsRes = appsFetched.concat(apps);
        this.setState({apps: appsRes , maybeMoreApps: maybeMoreAppsFetched, totalAppsFetched: totalAppsFetched + appsFetched.length},async ()  => {
            if(appsRes.length < MIN_APPS_NEEDED_TO_SCROLL && maybeMoreAppsFetched){
                await this._handleApplications();
            }
        });

        return {apps: apps, maybeMoreApps: maybeMoreApps}
    };

    _handleMoreApplications = async () => {
        const {totalAppsFetched, apps} =  this.state;
        const {apps: appsFetched, maybeMoreApps} = await this._fetchApplications();
        this.setState({apps: appsFetched.concat(apps), totalAppsFetched: totalAppsFetched + appsFetched.length, maybeMoreApps: maybeMoreApps})
    };

    render() {
        let {error, apps} = this.state;
        return <header className="dropdown-header">
            <form className="form flex-row" onSubmit={this.onSubmit}>
                <Select
                    valueKey={'id'}
                    className="select add-instance-select"
                    name="app"
                    value={this.props.app}
                    labelKey="name"
                    onChange={this.onOptionChange}
                    options={apps}
                    placeholder={i18n._('organization.desc.applications')}
                    required={true}
                    onMenuScrollToBottom={this._handleMoreApplications}
                />

                <div className="options flex-row">
                    {
                        !this.state.isLoading ?
                        <button type="submit" className="btn btn-submit" disabled={this.state.isLoading}>
                            {i18n._('store.install')}
                        </button> : null
                    }

                    {
                        this.state.isLoading &&
                        <i className="fa fa-spinner fa-spin option-icon" style={{ 'marginLeft': '1em' }}/>
                    }
                </div>

            </form>

            <NotificationMessageBlock type={'danger'}
                                      display={error.message !== ''}
                                      close={() => this.setState(prevState => ({error : {...prevState.error, message: ''}}))}
                                      message={error.message + ' ('+ i18n._('error-code')+' : ' + error.http_status+')'}/>
        </header>;
    }
}

const styles = {
    displayMore :{
        position: "absolute",
        height: "50%",
        width: "15%",
        display: "flex",
        alignItems:"flex-end",
        justifyContent: "center"
    }
};

export default AddInstanceDropdownHeader;
