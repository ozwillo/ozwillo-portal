import { combineReducers } from 'redux';
import { i18nState } from 'redux-i18n';

//Reducers
import appStore from './app-store';
import config from './config';
import instance from './instance';
import member from './member';
import notifications from './notifications';
import organization from './organization';
import userInfo from './userInfo';

//Components
import organizationForm from './components/organization-form';

export default combineReducers({
    appStore,
    config,
    instance,
    member,
    notifications,
    organization,
    userInfo,
    i18nState,

    //Components
    organizationForm
});