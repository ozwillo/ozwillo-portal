import { FETCH_USERS_OF_SERVICE } from "../../actions/service";
import {
    FETCH_CREATE_ACL,
    FETCH_DELETE_ACL,
} from "../../actions/acl";

const defaultState = {
    users: []
};

export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    switch (action.type) {
        case FETCH_USERS_OF_SERVICE:
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