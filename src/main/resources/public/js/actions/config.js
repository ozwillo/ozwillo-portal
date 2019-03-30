import {setLanguage, setTranslations} from "redux-i18n";
import customFetch, {urlBuilder} from "../util/custom-fetch";
import { i18n } from "../config/i18n-config"


export const FETCH_CONFIG = 'FETCH_CONFIG';
export const FETCH_SET_LANGUAGE = 'FETCH_SET_LANGUAGE';

// actions
export const fetchConfigAction = (config) => {
    return {
        type: FETCH_CONFIG,
        config
    };
};

export const setLanguageAction = (config) => {
    return {
        type: FETCH_SET_LANGUAGE,
        config
    };
};

// Async methods
export const fetchConfig = () => {
    return (dispatch) => {
        return customFetch('/api/config')
            .then((res) => {
                //Config
                dispatch(fetchConfigAction(res));

                i18n.activate(res.language);
            })
    };
};

export const fetchCountries = (q = '') => {
    return (dispatch) => {
        return customFetch(urlBuilder('/api/geo/countries', {q}))
            .then((res) => {
                dispatch(fetchConfigAction({countries: res.areas}));
            });
    };
};

export const fetchSetLanguage= (language) => {
    return (dispatch, getState) => {

        const state = getState();
        if (state.config.language === language) {
            return;
        }

        if(state.i18nState.translations[language]) {
            //Config
            dispatch(setLanguageAction({ language }));

            //i18n-redux
            dispatch(setLanguage(language));
            return;
        }

        return customFetch(`/api/config/language/${language}`)
            .then(({ siteMapFooter }) => {
                //Config
                dispatch(fetchConfigAction({ language, siteMapFooter }));

                //i18n-redux
                dispatch(setLanguage(language));
                dispatch(setLanguageAction(language));
                i18n.activate(language);
            });
    };
};

export const fetchCsrf = () => {
    return (dispatch) => {
        return fetch('/api/csrf-token', {
            credentials : 'same-origin'
        }).then((res) => {
            dispatch(fetchConfigAction({'csrfHeader': res.headers.get('X-CSRF-HEADER'),
                'csrfToken': res.headers.get('X-CSRF-TOKEN')}));
        })
    };
};
