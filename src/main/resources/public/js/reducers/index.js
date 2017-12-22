import { combineReducers } from 'redux';
import { i18nState } from 'redux-i18n';

//Reducers
import config from './config';
import notifications from './notifications';
import userInfo from './userInfo';

export default combineReducers({
    config,
    notifications,
    userInfo,
    i18nState
});