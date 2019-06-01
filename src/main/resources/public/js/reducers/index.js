import {combineReducers} from 'redux';
import {i18nState} from 'redux-i18n';

//Reducers
import config from './config';
import organization from './organization';
import userInfo from './user-info';

//Components
import organizationForm from './components/organization-form';

export default combineReducers({
    config,
    organization,
    userInfo,
    i18nState,

    //Components
    organizationForm
});
