import React from 'react';
import { connect } from 'react-redux';
import { Route } from 'react-router-dom';
import { Router, Switch, Redirect } from 'react-router';
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
import Network from '../pages/network';
import Apps from '../pages/myapps';
import Notification from '../pages/notifications';
import OrganizationCreate from '../pages/organization-create';
import OrganizationSearch from '../pages/organization-search';
import OrganizationDesc from '../pages/organization-desc';
import Store from '../pages/store';

// Actions
import { fetchUserInfo } from "../actions/user";

class RouterWithUser extends React.Component {
    render() {
        return <IfUser>
            <NotificationsCount/>
            <Switch>
                <Route path="/my/profile/franceconnect" component={SynchroniseFCProfile}/>
                <Route path="/my/profile" component={Profile}/>
                <Route path="/my/network" component={Network}/>
                <Route path="/my/apps" component={Apps}/>
                <Route path="/my/notif" component={Notification}/>
                <Route path="/my/organization/create" component={OrganizationCreate}/>
                <Route path="/my/organization/:id/:tab?" component={OrganizationDesc}/>
                <Route path="/my/organization" component={OrganizationSearch}/>
                <Route path="/my/" component={Dashboard}/>
                <Redirect to="/my/"/>
            </Switch>
        </IfUser>;
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
                <Layout>
                    <Popup/>
                    <Switch>
                        <Route path="/my" component={RouterWithUser} />
                        <Route path="/:lang/store" component={Store}/>
                        <Redirect to="/my/"/>
                    </Switch>
                </Layout>
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
