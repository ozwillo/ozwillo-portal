import {
    FETCH_USER_ORGANIZATIONS,
    FETCH_ORGANIZATION_WITH_ID,
    FETCH_CREATE_ORGANIZATION
} from '../actions/organization';

const defaultState = {
    organizations: [],
    current: {
        instances: [],
        members: []
    }
};

const organizationsState = (state = [], action ) => {
    let nextState = Object.assign([], state);
    switch(action.type) {
        case FETCH_USER_ORGANIZATIONS:
            nextState = action.organizations;
            break;
        case FETCH_CREATE_ORGANIZATION:
            nextState.push(action.organization);
            break;
        default:
            return state;
    }

    return nextState;
};

export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    switch(action.type) {
        case FETCH_USER_ORGANIZATIONS:
        case FETCH_CREATE_ORGANIZATION:
            nextState.organizations = organizationsState(nextState.organizations, action);
            break;
        case FETCH_ORGANIZATION_WITH_ID:
            nextState.current = action.organization;
            break;
        default:
            return state;
    }
    
    return nextState;
}