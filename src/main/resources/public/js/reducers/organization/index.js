import {
    FETCH_USER_ORGANIZATIONS,
    FETCH_ORGANIZATION_WITH_ID,
    FETCH_CREATE_ORGANIZATION
} from '../../actions/organization';

import { FETCH_USERS_OF_SERVICE } from "../../actions/service";
import { FETCH_CREATE_ACL } from "../../actions/acl";

//Reducers
import serviceReducer from './service';

const defaultState = {
    organizations: [],
    current: {
        services: [],
        members: []
    }
};

const servicesState = (state = [], action) => {
    let nextState = Object.assign([], state);
    switch(action.type) {
        case FETCH_USERS_OF_SERVICE:
        case FETCH_CREATE_ACL:
            const i = nextState.findIndex((service) => {
                return service.catalogEntry.id === action.serviceId;
            });

            if(i < 0){
                return state;
            }

            nextState[i] = serviceReducer(nextState[i], action);
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
        case FETCH_CREATE_ACL:
        case FETCH_USERS_OF_SERVICE:
            nextState.services = servicesState(state.services, action);
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
        case FETCH_USERS_OF_SERVICE:
        case FETCH_CREATE_ACL:
        case FETCH_ORGANIZATION_WITH_ID:
            nextState.current = currentOrganizationState(state.current, action);
            break;
        default:
            return state;
    }
    
    return nextState;
}