import { combineReducers } from 'redux';
import { i18nState } from 'redux-i18n';

//Reducers
import config from './config';
import notifications from './notifications';

export default combineReducers({
    config,
    notifications,
    i18nState
});