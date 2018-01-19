import { FETCH_USERS_OF_SERVICE } from "../../actions/service";

const defaultState = {
    users: []
};

export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    switch (action.type) {
        case FETCH_USERS_OF_SERVICE:
            nextState.users = action.users;
            break;
        default:
            return state;
    }

    return nextState;
}