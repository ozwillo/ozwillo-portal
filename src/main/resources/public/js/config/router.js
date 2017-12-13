import React from 'react';
import { Route } from 'react-router-dom';
import { Router, Switch } from 'react-router';

import history from './history';

//Pages
import Dashboard from '../pages/dashboard';

class AppRouter extends React.Component {
    render() {
        return <Router history={history}>
            <Switch>
                <Route path="/(dasboard)?" component={Dashboard}/>
            </Switch>
        </Router>;
    }
}

export default AppRouter;
