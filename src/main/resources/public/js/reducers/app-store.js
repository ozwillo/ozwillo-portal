import {FETCH_APPLICATIONS} from '../actions/app-store';

const defaultState = {
    apps: []
};

export default (state = defaultState, action) => {
    const nextState = Object.assign({}, state);

    switch (action.type) {
        case FETCH_APPLICATIONS:
            nextState.apps = action.apps;
            break;
        default:
            return state;
    }

    return nextState;
}