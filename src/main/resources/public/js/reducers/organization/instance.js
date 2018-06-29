import {
    FETCH_CREATE_ACL,
    FETCH_DELETE_ACL
} from "../../actions/acl";

import {
    FETCH_USERS_OF_INSTANCE,
    FETCH_UPDATE_INSTANCE_STATUS,
    FETCH_UPDATE_SERVICE_CONFIG
} from "../../actions/instance";

import {
    FETCH_CREATE_SUBSCRIPTION,
    FETCH_DELETE_SUBSCRIPTION
} from '../../actions/subscription';

const defaultState = {
    services: [],
    users: []
};

import serviceState from './service';

export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    let i = -1;
    switch (action.type) {
        case FETCH_CREATE_SUBSCRIPTION:
        case FETCH_DELETE_SUBSCRIPTION:
        case FETCH_UPDATE_SERVICE_CONFIG:
            i = nextState.services.findIndex((service) => {
                return service.catalogEntry.id === action.service.catalogEntry.id;
            });

            if (i < 0) {
                return state;
            }

            nextState.services[i] = serviceState(nextState.services[i], action);
            break;
        case FETCH_UPDATE_INSTANCE_STATUS:
            nextState = Object.assign({}, state, action.instance);
            break;
        case FETCH_USERS_OF_INSTANCE:
            nextState.users = action.users;
            break;
        case FETCH_CREATE_ACL:
            nextState.users = Object.assign([], state.users);
            nextState.users.push(action.user);
            break;
        case FETCH_DELETE_ACL:
            nextState.users = Object.assign([], state.users);
            i = nextState.users.findIndex((user) => {
                //We check names if user is waiting before to accept a request
                return (!action.user.id && user.name === action.user.name) || user.id === action.user.id
            });
            nextState.users.splice(i, 1);
            break;
        default:
            return state;
    }

    return nextState;
}