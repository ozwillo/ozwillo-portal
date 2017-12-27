import { FETCH_ORGANIZATIONS } from "../actions/organization";

const defaultState = {
    organizations: []
};

export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    switch(nextState.id) {
        case FETCH_ORGANIZATIONS:
            nextState.organizations = action.organizations;
            break;
        default:
            return state;
    }
    
    return nextState;
}