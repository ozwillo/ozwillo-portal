import {setLanguage, setTranslations} from "redux-i18n";
import customFetch, {urlBuilder} from "../util/custom-fetch";

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
        return customFetch('/api/config.json')
            .then((res) => {
                // Language
                dispatch(setLanguage(res.language));
                dispatch(setTranslations(res.i18n));

                //Config
                dispatch(fetchConfigAction(res));
            })
    };
};

export const fetchMyConfig = () => {
    return (dispatch) => {
        return customFetch('/my/api/config.json')
            .then((res) => {
                // Language
                dispatch(setLanguage(res.language));
                dispatch(setTranslations(res.i18n));

                //Config
                dispatch(fetchConfigAction(res));
            })
    };
};

export const fetchCountries = (q = '') => {
    return (dispatch) => {
        return customFetch(urlBuilder('/api/store/dc-countries', {q}))
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
            .then(({ i18n, siteMapFooter, siteMapHeader }) => {
                //Config
                dispatch(fetchConfigAction({ language, siteMapFooter, siteMapHeader }));

                //i18n-redux
                dispatch(setLanguage(language));
                dispatch(setTranslations(i18n, {preserveExisting: true}));
            });
    };
};