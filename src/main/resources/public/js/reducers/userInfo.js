import {
    FETCH_USER_INFO
} from '../actions/user';

export default (state = {}, action) => {
    switch(action.type){
        case FETCH_USER_INFO:
            return action.userInfo;
            break;
        default:
            return state;
    }
}