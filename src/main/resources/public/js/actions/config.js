import {setLanguage, setTranslations} from "redux-i18n";
import customFetch from "../util/custom-fetch";

export const FETCH_CONFIG = 'FETCH_CONFIG';

export const fetchConfigAction = (config) => {
    return {
        type: FETCH_CONFIG,
        config
    };
};

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