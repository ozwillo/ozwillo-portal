import { setTranslations } from 'redux-i18n';
import customFetch from '../util/custom-fetch';

export const FETCH_SITE_MAP_FOOTER = 'FETCH_SITE_MAP_FOOTER';

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
                dispatch(setTranslations(config.i18n, config.languages));
                dispatch(fetchSiteMapFooterAction(config.siteMapFooter));
                return config;
            })
    };
};