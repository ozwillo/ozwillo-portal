import {FETCH_CONFIG, FETCH_SET_LANGUAGE} from '../actions/config';

const defaultState = {
    siteMapFooter: {},
    siteMapHeader: {},
    currentSiteMapFooter: null,
    currentSiteMapHeader: null,
    language: 'en',
    languages: ['en'],
    kernelEndPoint: '',
    accountEndPoint: '',
    opendatEndPoint: '',
    countries: []
};

export default (state = defaultState, action) => {
    const nextState = Object.assign({}, state, action.config);
    switch (action.type) {
        case FETCH_CONFIG:
            nextState.siteMapFooter = { ...state.siteMapFooter, [action.config.language]: action.config.siteMapFooter };
            nextState.siteMapHeader = { ...state.siteMapHeader, [action.config.language]: action.config.siteMapHeader };
        case FETCH_SET_LANGUAGE:
            nextState.currentSiteMapFooter = nextState.siteMapFooter[nextState.language];
            nextState.currentSiteMapHeader = nextState.siteMapHeader[nextState.language];
            break;
        default:
            return state;
    }
    return nextState;
}