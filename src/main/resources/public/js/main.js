import React from 'react';
import ReactDOM from 'react-dom';

import { Provider } from 'react-redux';
import store from './util/store';
import I18n from "redux-i18n"

import App from './app';
import BootLoader from './components/boot-loader';
import NotificationsCountLoader from './components/notifications-count-loader';

ReactDOM.render(
    <Provider store={store}>
        <I18n translations={{}} initialLang="en" useReducer={true}>
            <BootLoader/>
            <NotificationsCountLoader />
            <App />
        </I18n>
    </Provider>,
    document.getElementById('root')
);