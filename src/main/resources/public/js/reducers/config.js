import { FETCH_SITE_MAP_FOOTER } from '../actions/config';

const siteMapFooter = (state = {}, action) => {
    let nextState = Object.assign({}, state);
    switch(action.type) {
        case FETCH_SITE_MAP_FOOTER:
            nextState = action.siteMapFooter;
            break;
        default:
            nextState = state;
    }

    return nextState;
};

const defaultState = {
    siteMapFooter: null
};

export default (state = defaultState, action) => {
    switch(action.type) {
        case FETCH_SITE_MAP_FOOTER:
            const nextState = Object.assign({}, state);
            nextState.siteMapFooter = siteMapFooter(state.siteMapFooter, action);
            return nextState;
        default:
            return state;
    }
}