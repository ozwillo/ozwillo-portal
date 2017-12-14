import React from 'react';
import ReactDOM from 'react-dom';

import { Provider } from 'react-redux';
import store from './util/store';
import I18n from "redux-i18n"

import App from './app';
import I18nLoader from './components/config-loader';

ReactDOM.render(
    <Provider store={store}>
        <I18n translations={{}} initialLang="en" useReducer={true}>
            <I18nLoader/>
            <App />
        </I18n>
    </Provider>,
    document.getElementById('root')
);