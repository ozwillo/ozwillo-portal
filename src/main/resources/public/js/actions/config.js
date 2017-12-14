import { setTranslations } from 'redux-i18n';
import customFetch from '../util/custom-fetch';

export const fetchConfig = () => {
    return (dispatch) => {
        //Request
        return customFetch('/my/api/config.json')
            .then((config) => {
                dispatch(setTranslations(config.i18n, config.languages));
                return config;
            })
    };
};