import { combineReducers } from 'redux';
import { i18nState } from 'redux-i18n';

//Reducers
import config from './config';

export default combineReducers({
    config,
    i18nState
});