import { FETCH_SITE_MAP_FOOTER, FETCH_LANGUAGE } from '../actions/config';

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
    siteMapFooter: null,
    language: 'en'
};

export default (state = defaultState, action) => {
    const nextState = Object.assign({}, state);
    switch(action.type) {
        case FETCH_SITE_MAP_FOOTER:
            nextState.siteMapFooter = siteMapFooter(state.siteMapFooter, action);
        case FETCH_LANGUAGE:
            nextState.language = action.language;
        default:
            return state;
    }
    return nextState;
}