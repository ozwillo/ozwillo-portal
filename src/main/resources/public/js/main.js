import React from 'react';
import ReactDOM from 'react-dom';

import {Provider} from 'react-redux';
import store from './util/store';

import App from './app';

//Load plugins
import './util/string-plugin';

ReactDOM.render(
    <Provider store={store}>
            <App/>
    </Provider>,
    document.getElementById('root')
);