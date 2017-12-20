import { FETCH_CONFIG } from '../actions/config';

const defaultState = {
    siteMapFooter: null,
    language: 'en',
    kernelEndPoint: '',
    accountEndPoint: ''
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