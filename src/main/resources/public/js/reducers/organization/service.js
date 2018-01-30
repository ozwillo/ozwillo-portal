import { FETCH_USERS_OF_SERVICE } from "../../actions/service";
import { FETCH_CREATE_ACL } from "../../actions/acl";

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
        default:
            return state;
    }

    return nextState;
}