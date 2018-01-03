import {
    FETCH_ORGANIZATIONS,
    FETCH_ORGANIZATION_WITH_ID
} from "../actions/organization";

const defaultState = {
    organizations: [],
    current: {
        members: [],
        services: [
            { id: 1, name: "App 1", isPublic: true, members: [] },
            { id: 2, name: "App 2", isPublic: false, members: [] }
        ]
    },
    newOrganization: {
        members: [],
        services: []
    }
};

export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    switch(nextState.id) {
        case FETCH_ORGANIZATIONS:
            nextState.organizations = action.organizations;
            break;
        case FETCH_ORGANIZATION_WITH_ID:
            nextState.current = action.organization;
            break;
        default:
            return state;
    }
    
    return nextState;
}