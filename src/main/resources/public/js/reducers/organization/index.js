import {
    FETCH_USER_ORGANIZATIONS,
    FETCH_ORGANIZATION_WITH_ID,
    FETCH_CREATE_ORGANIZATION
} from '../../actions/organization';

import {
    FETCH_CREATE_ACL,
    FETCH_DELETE_ACL
} from "../../actions/acl";

import { FETCH_USERS_OF_INSTANCE } from "../../actions/instance";

//Reducers
import instanceReducer from './instance';

const defaultState = {
    organizations: [],
    current: {
        instances: [],
        services: [],
        members: []
    }
};

const instancesState = (state = [], action) => {
    let nextState = Object.assign([], state);
    switch(action.type) {
        case FETCH_USERS_OF_INSTANCE:
        case FETCH_DELETE_ACL:
        case FETCH_CREATE_ACL:
            const i = nextState.findIndex((instance) => {
                return instance.id === action.instanceId;
            });

            if(i < 0){
                return state;
            }

            nextState[i] = instanceReducer(nextState[i], action);
            break;
        default:
            return state;
    }
    return nextState;
};


const currentOrganizationState = (state = {}, action) => {
    let nextState = Object.assign({}, state);
    switch(action.type) {
        case FETCH_ORGANIZATION_WITH_ID:
            nextState = action.organization;
            break;
        case FETCH_DELETE_ACL:
        case FETCH_CREATE_ACL:
        case FETCH_USERS_OF_INSTANCE:
            nextState.instances = instancesState(state.instances, action);
            break;
        default:
            return state;
    }

    return nextState;
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
        case FETCH_USERS_OF_INSTANCE:
        case FETCH_CREATE_ACL:
        case FETCH_DELETE_ACL:
        case FETCH_ORGANIZATION_WITH_ID:
            nextState.current = currentOrganizationState(state.current, action);
            break;
        default:
            return state;
    }
    
    return nextState;
}