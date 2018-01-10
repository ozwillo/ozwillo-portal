import {
    FETCH_ORGANIZATION_WITH_ID,
    FETCH_CREATE_ORGANIZATION
} from '../actions/organization';

const defaultState = {
    organizations: [
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
    ],
    current: {
        id: '2e6039b5-85d5-46b7-95f9-9fff77738626',
        members: [
            {
                id: 1,
                firstname: 'firstname',
                lastname: 'lastname',
                services: [
                    {
                        id: 1,
                        name: 'App 1'
                    },
                    {
                        id: 2,
                        name: 'App 2'
                    },
                    {
                        id: 3,
                        name: 'App 3'
                    }
                ]
            },
            {
                id: 2,
                firstname: 'firstname 2',
                lastname: 'lastname 2',
                services: [
                    {
                        id: 1,
                        name: 'App 1'
                    },
                    {
                        id: 3,
                        name: 'App 3'
                    }
                ]
            },
            {
                id: 3,
                firstname: 'firstname 3',
                lastname: 'lastname 3',
                services: [],
                isPending: true
            }
        ],
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
    newOrganization: {
        members: [],
        services: []
    }
};

const organizationsState = (state = [], action ) => {
    let nextState = Object.assign([], state);
    switch(action.type) {
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
    switch(nextState.id) {
        case FETCH_CREATE_ORGANIZATION:
            nextState.organizations = organizationsState(nextState.organizations, action);
            break;
        case FETCH_ORGANIZATION_WITH_ID:
            nextState.current = action.organization;
            break;
        default:
            return state;
    }
    
    return nextState;
}