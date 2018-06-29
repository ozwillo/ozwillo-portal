import {FETCH_USER_INFO} from '../actions/user';


const defaultState = {};
export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    switch (action.type) {
        case FETCH_USER_INFO:
            return Object.assign(nextState, action.userInfo);
            break;
        default:
            return state;
    }

    return nextState;
}