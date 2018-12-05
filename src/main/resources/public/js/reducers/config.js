import {FETCH_CONFIG, FETCH_SET_LANGUAGE} from '../actions/config';

const defaultState = {
    siteMapFooter: {},
    currentSiteMapFooter: null,
    language: 'en',
    languages: ['en'],
    kernelEndPoint: '',
    accountEndPoint: '',
    opendataEndPoint: '',
    countries: [],
    csrfHeader: '',
    csrfToken: ''
};

export default (state = defaultState, action) => {
    const nextState = Object.assign({}, state, action.config);
    switch (action.type) {
        case FETCH_CONFIG:
            nextState.siteMapFooter = { ...state.siteMapFooter, [action.config.language]: action.config.siteMapFooter };
            break;
        case FETCH_SET_LANGUAGE:
            nextState.currentSiteMapFooter = nextState.siteMapFooter[nextState.language];
            break;
        default:
            return state;
    }
    return nextState;
}