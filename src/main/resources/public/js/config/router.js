import React from 'react';
import { Route } from 'react-router-dom';
import { Router, Switch, Redirect} from 'react-router';
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
import Store from '../pages/store';


class RouterWithUser extends React.Component {
    render() {
        return <IfUser>
            <Layout>
                <BootLoader />
                <NotificationsCount/>
                <Switch>
                    <Route path="/my/profile/franceconnect" component={SynchroniseFCProfile}/>
                    <Route path="/my/profile" component={Profile}/>
                    <Route path="/my/network" component={Network}/>
                    <Route path="/my/apps" component={Apps}/>
                    <Route path="/my/notif" component={Notification}/>
                    <Route path="/:lang/store" component={Store}/>
                    <Route path="/my/(dashboard)?" component={Dashboard}/>
                </Switch>
            </Layout>
        </IfUser>
    }
}

class PublicRouter extends React.Component {
    render() {
        return <Layout>
                <BootLoader />
                <Route path="/:lang/store" component={Store}/>
            </Layout>;
    }
}


class AppRouter extends React.Component {
    render() {
        return <Router history={history}>
            <Switch>
                <Route path="/:lang/store" component={PublicRouter}/>
                <Route component={RouterWithUser} />
            </Switch>
        </Router>;
    }
}

export default AppRouter;
