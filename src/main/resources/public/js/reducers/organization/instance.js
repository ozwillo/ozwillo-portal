import {
    FETCH_CREATE_ACL,
    FETCH_DELETE_ACL
} from "../../actions/acl";

import {
    FETCH_USERS_OF_INSTANCE,
    FETCH_UPDATE_INSTANCE_STATUS
} from "../../actions/instance";

const defaultState = {
    services: [],
    users: []
};


export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    switch(action.type) {
        case FETCH_UPDATE_INSTANCE_STATUS:
            nextState.applicationInstance = Object.assign({}, state.applicationInstance, { status: action.status });
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
            const i = nextState.users.findIndex((user) => {
                //We check names if user is waiting before to accept a request
                return  (!action.user.id && user.name === action.user.name ) || user.id === action.user.id
            });
            nextState.users.splice(i, 1);
            break;
        default:
            return state;
    }

    return nextState;
}