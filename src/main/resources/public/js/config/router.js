import React from 'react';
import { Route } from 'react-router-dom';
import { Router, Switch } from 'react-router';

import history from './history';

import Layout from '../components/layout';

//Pages
import Dashboard from '../pages/dashboard';
import Profile from '../pages/profile';
import SynchroniseFCProfile from '../pages/synchronize-fc-profile';
import Network from '../pages/network';
import Apps from '../pages/myapps';
import Notification from '../pages/notifications';

class AppRouter extends React.Component {
    render() {
        return <Router history={history}>
            <Layout>
                <Switch>
                    <Route path="/my/profile/franceconnect" component={SynchroniseFCProfile}/>
                    <Route path="/my/profile" component={Profile}/>
                    <Route path="/my/network" component={Network}/>
                    <Route path="/my/apps" component={Apps}/>
                    <Route path="/my/notif" component={Notification}/>
                    <Route path="/(my/)?(dashboard)?" component={Dashboard}/>
                </Switch>
            </Layout>
        </Router>;
    }
}

export default AppRouter;
