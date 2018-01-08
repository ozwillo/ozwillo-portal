import { FETCH_CONFIG } from '../actions/config';

const defaultState = {
    siteMapFooter: null,
    siteMapHeader: null,
    language: 'en',
    languages: ['en'],
    kernelEndPoint: '',
    accountEndPoint: '',
    opendatEndPoint: '',
    countries: []
};

export default (state = defaultState, action) => {
    switch(action.type) {
        case FETCH_CONFIG:
            return Object.assign({}, state, action.config);
            break;
        default:
            return state;
    };
}