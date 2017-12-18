import { setTranslations, setLanguage } from 'redux-i18n';
import customFetch from '../util/custom-fetch';

export const FETCH_SITE_MAP_FOOTER = 'FETCH_SITE_MAP_FOOTER';
export const FETCH_LANGUAGE = 'FETCH_LANGUAGE';

const fetchSiteMapFooterAction = (siteMapFooter) => {
    return {
        type: FETCH_SITE_MAP_FOOTER,
        siteMapFooter
    }
}

export const fetchConfig = () => {
    return (dispatch) => {
        //Request
        return customFetch('/my/api/config.json')
            .then((config) => {
                // Language
                dispatch(setLanguage(config.language));
                dispatch(setTranslations(config.i18n));

                //site map footer
                dispatch(fetchSiteMapFooterAction(config.siteMapFooter));
                return config;
            })
    };
};