import {createStore, applyMiddleware, compose} from 'redux';

import thunkMiddleware from 'redux-thunk';
import reducers from '../reducers';

const composeEnhancers =
    typeof window === 'object' &&
    window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ ?
        window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__({
            // Specify extension’s options like name, actionsBlacklist, actionsCreators, serialize...
        }) : compose;

const enhancer = composeEnhancers(
    applyMiddleware(thunkMiddleware), // create async actions
);

const store = createStore(reducers, enhancer);

export default store;