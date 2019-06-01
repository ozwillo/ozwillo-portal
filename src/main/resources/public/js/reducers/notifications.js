import {
    FETCH_NOTIFICATIONS_COUNT,
} from '../actions/notifications';

const defaultState = {
    message: '',
    count: 0
};


export default (state = defaultState, action) => {
    const nextState = Object.assign({}, state);

    switch (action.type) {
        case FETCH_NOTIFICATIONS_COUNT:
            nextState.count = action.count;
            break;
        default:
            return state;
    }

    return nextState;
}
