import { FETCH_USER_INFO } from '../actions/user';
import { FETCH_CREATE_ORGANIZATION, FETCH_USER_ORGANIZATIONS } from "../actions/organization";


const defaultState = {
    organizations: []
};

//TODO: Remove organizations insert for tests
const organizations = [
    {
        id: '1',
        name: 'Organization 1',
        members: [],
        services: [
            { id: 1, name: 'App 1', isPublic: true, members: [
                    {
                        id: 1,
                        firstname: 'firstname',
                        lastname: 'lastname'
                    }
                ] },
            { id: 2, name: 'App 2', isPublic: false, members: [
                    {
                        id: 1,
                        firstname: 'firstname',
                        lastname: 'lastname'
                    }
                ] }
        ]
    },
    {
        id: '2',
        name: 'Organization 2',
        members: [],
        services: [
            { id: 1, name: 'App 1', isPublic: true, members: [
                    {
                        id: 1,
                        firstname: 'firstname',
                        lastname: 'lastname'
                    }
                ] },
            { id: 2, name: 'App 2', isPublic: false, members: [
                    {
                        id: 1,
                        firstname: 'firstname',
                        lastname: 'lastname'
                    }
                ] }
        ]
    }
];

const organizationsState = (state = [], action ) => {
    let nextState = Object.assign([], state);
    switch(action.type) {
        case FETCH_USER_ORGANIZATIONS:
            //TODO: Remove organizations insert for tests
            nextState = action.organizations.concat(organizations);
            break;
        case FETCH_CREATE_ORGANIZATION:
            nextState.push(action.organization);
            break;
        default:
            return state;
    }

    return nextState;
};

export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    switch(action.type){
        case FETCH_USER_INFO:
            return Object.assign(nextState, action.userInfo);
            break;
        case FETCH_USER_ORGANIZATIONS:
        case FETCH_CREATE_ORGANIZATION:
            nextState.organizations = organizationsState(state.organizations, action);
            break;
        default:
            return state;
    }

    return nextState;
}