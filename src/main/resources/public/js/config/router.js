import React from 'react';
import {connect} from 'react-redux';
import {Route} from 'react-router-dom';
import {Router, Switch, Redirect} from 'react-router';
import Popup from 'react-popup';
import history from './history';

// Components
import IfUser from '../components/IfUser';
import Layout from '../components/layout';
import BootLoader from '../components/boot-loader';
import NotificationsCount from '../components/notifications-count-loader';

//Pages
import Dashboard from '../pages/dashboard';
import Profile from '../pages/profile';
import SynchroniseFCProfile from '../pages/synchronize-fc-profile';
import Notification from '../pages/notifications';
import OrganizationCreate from '../pages/organization-create';
import OrganizationSearch from '../pages/organization-search';
import OrganizationDesc from '../pages/organization-desc';
import AppStore from '../pages/app-store';

// Actions
import {fetchUserInfo} from "../actions/user";
import AppInstall from "../pages/app-install";

class RouterWithUser extends React.Component {
    render() {
        return <IfUser>
            <NotificationsCount/>
            <Layout>
                <Switch>
                    {/* Redirect old pages */}
                    <Redirect from="/my/network" to="/my/organization"/>
                    <Redirect from="/my/apps" to="/my/organization"/>

                    {/* Routes */}
                    <Route path="/my/profile/franceconnect" component={SynchroniseFCProfile}/>
                    <Route path="/my/profile" component={Profile}/>
                    <Route path="/my/notif" component={Notification}/>
                    <Route path="/my/organization/create" component={OrganizationCreate}/>
                    <Route path="/my/organization/:id/:tab?" component={OrganizationDesc}/>
                    <Route path="/my/organization" component={OrganizationSearch}/>
                    <Route path="/my/dashboard/:id?" component={Dashboard}/>
                    <Route path="/my/" component={Dashboard}/>
                    <Redirect to="/my/"/>
                </Switch>
            </Layout>
        </IfUser>;
    }
}

class PopupRouterWithUser extends React.Component {
    render() {
        return <IfUser>
            <Switch>
                <Route path="/popup/profile" component={Profile}/>
            </Switch>
        </IfUser>;
    }
}

class RouterWithoutUser extends React.Component {
    render() {
        return <Layout>
            <Switch>
                <Route path="/:lang/store/:type/:id" component={AppInstall}/>
                <Route path="/:lang/store" component={AppStore}/>
            </Switch>
        </Layout>;
    }
}

class AppRouter extends React.Component {

    constructor(props) {
        super(props);

        history.listen(() => {
            this.props.fetchUserInfo();
        });
    }

    componentDidMount() {
        this.props.fetchUserInfo();
    }

    render() {
        return <Router history={history}>
            <BootLoader>
                <Popup/>
                <Switch>
                    <Route path="/my" component={RouterWithUser}/>
                    <Route path="/popup" component={PopupRouterWithUser}/>
                    <Route path="/:lang/store" component={RouterWithoutUser}/>
                    <Redirect to="/my"/>
                </Switch>
            </BootLoader>
        </Router>;
    }
}

const mapStateToProps = state => {
    return {
        userInfo: state.userInfo
    };
};

const mapDispatchToProps = dispatch => {
    return {
        fetchUserInfo() {
            return dispatch(fetchUserInfo());
        }
    };
};

export default connect(mapStateToProps, mapDispatchToProps)(AppRouter);
