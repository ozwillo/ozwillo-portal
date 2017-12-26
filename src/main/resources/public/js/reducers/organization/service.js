import {
    FETCH_CREATE_SUBSCRIPTION,
    FETCH_DELETE_SUBSCRIPTION
} from '../../actions/subscription';
import {FETCH_UPDATE_SERVICE_CONFIG} from "../../actions/instance";


export default (state = {}, action) => {
    let nextState = Object.assign({}, state);

    switch (action.type) {
        case FETCH_UPDATE_SERVICE_CONFIG:
            nextState = action.service;
            break;
        case FETCH_CREATE_SUBSCRIPTION:
            nextState.subscriptions = Object.assign([], nextState.subscriptions);
            nextState.subscriptions.push(action.sub);
            break;
        case FETCH_DELETE_SUBSCRIPTION:
            const i = nextState.subscriptions.findIndex((sub) => {
                return sub.id === action.sub.id;
            });

            if (i < 0) {
                return state;
            }

            nextState.subscriptions = Object.assign([], nextState.subscriptions);
            nextState.subscriptions.splice(i, 1);
            break;
        default:
            return state;
    }

    return nextState;
}