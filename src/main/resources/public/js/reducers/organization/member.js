import {FETCH_UPDATE_ROLE_MEMBER} from '../../actions/member';


export default (state = {}, action) => {
    let nextState = Object.assign({}, state);

    switch (action.type) {
        case FETCH_UPDATE_ROLE_MEMBER:
            nextState.admin = action.isAdmin;
            break;
        default:
            return state;
    }

    return nextState;
};