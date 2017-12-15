import React from 'react';
import { Route } from 'react-router-dom';
import { Router, Switch } from 'react-router';

import history from './history';

//Pages
import Dashboard from '../pages/dashboard';
import Profile from '../pages/profile';

class AppRouter extends React.Component {
    render() {
        return <Router history={history}>
            <Switch>
                <Route path="/(my/)?(dasboard)?" component={Dashboard}/>
                <Route path="/my/profile" component={Profile}/>
            </Switch>
        </Router>;
    }
}

export default AppRouter;
