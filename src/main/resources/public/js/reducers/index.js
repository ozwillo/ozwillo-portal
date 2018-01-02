import { combineReducers } from 'redux';
import { i18nState } from 'redux-i18n';

//Reducers
import config from './config';
import member from './member';
import notifications from './notifications';
import organization from './organization';
import service from './service';
import userInfo from './userInfo';

export default combineReducers({
    config,
    member,
    notifications,
    organization,
    service,
    userInfo,
    i18nState
});