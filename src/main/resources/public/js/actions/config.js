export const FETCH_SITE_MAP_FOOTER = 'FETCH_SITE_MAP_FOOTER';
export const FETCH_LANGUAGE = 'FETCH_LANGUAGE';

export const fetchSiteMapFooterAction = (siteMapFooter) => {
    return {
        type: FETCH_SITE_MAP_FOOTER,
        siteMapFooter
    }
};