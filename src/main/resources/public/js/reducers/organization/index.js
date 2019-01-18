import {
    FETCH_USER_ORGANIZATIONS,
    FETCH_ORGANIZATION_WITH_ID,
    FETCH_UPDATE_ORGANIZATION,
    FETCH_ORGANIZATION_INFO,
    FETCH_ORGANIZATION_MEMBERS
} from '../../actions/organization';

import {
    FETCH_CREATE_ACL,
    FETCH_DELETE_ACL
} from '../../actions/acl';

import {
    FETCH_UPDATE_INSTANCE_STATUS,
    FETCH_UPDATE_SERVICE_CONFIG
} from '../../actions/instance';

import {
    FETCH_UPDATE_ROLE_MEMBER
} from '../../actions/member';

import {FETCH_ADD_INSTANCE_TO_ORG} from '../../actions/app-store';

import {
    FETCH_CREATE_ORGANIZATION_INVITATION,
} from '../../actions/invitation';

//Reducers
import memberReducer from './member';

const defaultState = {
    organizations: [],
    current: {
        instances: [],
        services: [],
        members: []
    }
};

const instancesState = (state = [], action) => {
    let nextState = Object.assign([], state);
    switch (action.type) {
        case FETCH_ADD_INSTANCE_TO_ORG:
            nextState.push(action.instance);
            break;
        case FETCH_UPDATE_SERVICE_CONFIG:
        case FETCH_UPDATE_INSTANCE_STATUS:
        case FETCH_DELETE_ACL:
        case FETCH_CREATE_ACL:
        default:
            return state;
    }
    return nextState;
};

const membersState = (state = [], action) => {
    let nextState = Object.assign([], state);
    let i;
    switch (action.type) {
        case FETCH_UPDATE_ROLE_MEMBER:
            i = nextState.findIndex(member => {
                return member.id === action.memberId
            });

            if (!i) {
                return state;
            }

            nextState[i] = memberReducer(nextState[i], action);
            break;
        case FETCH_CREATE_ORGANIZATION_INVITATION:
            nextState.push(action.invitation);
            break;
        default:
            return state;
    }
    return nextState;
};


const currentOrganizationState = (state = {}, action) => {
    let nextState = Object.assign({}, state);
    switch (action.type) {
        case FETCH_ORGANIZATION_INFO:
            nextState.info = action.info;
            break;
        case FETCH_UPDATE_ORGANIZATION:
        case FETCH_ORGANIZATION_WITH_ID:
            nextState = action.organization;
            break;
        case FETCH_ADD_INSTANCE_TO_ORG:
        case FETCH_UPDATE_SERVICE_CONFIG:
        case FETCH_UPDATE_INSTANCE_STATUS:
        case FETCH_DELETE_ACL:
        case FETCH_CREATE_ACL:
        case FETCH_UPDATE_ROLE_MEMBER:
        case FETCH_CREATE_ORGANIZATION_INVITATION:
            nextState.members = membersState(nextState.members, action);
            break;
        default:
            return state;
    }

    return nextState;
};

const organizationsState = (state = [], action) => {
    let nextState = Object.assign([], state);
    switch (action.type) {
        case FETCH_USER_ORGANIZATIONS:
        default:
            return state;
    }

    return nextState;
};

export default (state = defaultState, action) => {
    let nextState = Object.assign({}, state);
    switch (action.type) {
        case FETCH_USER_ORGANIZATIONS:
        case FETCH_UPDATE_ROLE_MEMBER:
        case FETCH_UPDATE_SERVICE_CONFIG:
        case FETCH_ADD_INSTANCE_TO_ORG:
        case FETCH_UPDATE_INSTANCE_STATUS:
            // nextState.current.instances = action.
            for(let instance of nextState.current.instances){
                if(instance.id === action.instanceId){
                    instance.applicationInstance = action.instance.applicationInstance
                }
            }
        case FETCH_CREATE_ACL:
        case FETCH_DELETE_ACL:
        case FETCH_ORGANIZATION_WITH_ID:
        case FETCH_ORGANIZATION_INFO:
        case FETCH_UPDATE_ORGANIZATION:
        case FETCH_CREATE_ORGANIZATION_INVITATION:
            nextState.current = currentOrganizationState(state.current, action);
            break;
        case FETCH_ORGANIZATION_MEMBERS:
            nextState.current.members = action.members;
            break;
        default:
            return state;
    }

    return nextState;
}
