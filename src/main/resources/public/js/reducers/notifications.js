import {
    FETCH_NOTIFICATIONS,
    FETCH_NOTIFICATIONS_COUNT,
    DELETE_NOTIFICATION
} from '../actions/notifications';

const defaultState = {
    message: '',
    count: 0,
    notifications: [],
    apps: []
};


export default (state = defaultState, action) => {
    const nextState = Object.assign({}, state);

    switch (action.type) {
        case FETCH_NOTIFICATIONS_COUNT:
            nextState.message = action.message;
            nextState.count = action.count;
            break;
        case FETCH_NOTIFICATIONS:
            nextState.notifications = action.notifications;
            nextState.apps = action.apps;
            break;
        case DELETE_NOTIFICATION:
            nextState.notifications = state.notifications.filter((n) => {
                return n.id === action.id;
            });
            break;
        default:
            return state;
    }

    return nextState;
}