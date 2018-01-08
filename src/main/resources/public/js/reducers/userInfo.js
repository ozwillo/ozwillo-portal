import { FETCH_USER_INFO } from '../actions/user';
import { FETCH_USER_ORGANIZATIONS } from "../actions/organization";


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

export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    switch(action.type){
        case FETCH_USER_INFO:
            return Object.assign(nextState, action.userInfo);
            break;
        case FETCH_USER_ORGANIZATIONS:
            //TODO: Remove organizations insert for tests
            nextState.organizations = action.organizations.concat(organizations);
            break;
        default:
            return state;
    }

    return nextState;
}