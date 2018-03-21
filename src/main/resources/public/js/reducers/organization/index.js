import {
    FETCH_USER_ORGANIZATIONS,
    FETCH_USER_ORGANIZATIONS_LAZY_MODE,
    FETCH_ORGANIZATION_WITH_ID,
    FETCH_CREATE_ORGANIZATION,
    FETCH_UPDATE_ORGANIZATION,
    FETCH_ORGANIZATION_INFO,
    FETCH_UPDATE_STATUS_ORGANIZATION
} from '../../actions/organization';

import {
    FETCH_CREATE_ACL,
    FETCH_DELETE_ACL
} from '../../actions/acl';

import {
    FETCH_USERS_OF_INSTANCE,
    FETCH_UPDATE_INSTANCE_STATUS,
    FETCH_UPDATE_SERVICE_CONFIG
} from '../../actions/instance';

import {
    FETCH_DELETE_MEMBER,
    FETCH_UPDATE_ROLE_MEMBER
} from '../../actions/member';

import {FETCH_ADD_INSTANCE_TO_ORG} from '../../actions/app-store';

import {
    FETCH_CREATE_SUBSCRIPTION,
    FETCH_DELETE_SUBSCRIPTION
} from '../../actions/subscription';

import {
    FETCH_CREATE_ORGANIZATION_INVITATION,
    FETCH_DELETE_ORGANIZATION_INVITATION
} from '../../actions/invitation';

//Reducers
import instanceReducer from './instance';
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
        case FETCH_USERS_OF_INSTANCE:
        case FETCH_DELETE_ACL:
        case FETCH_CREATE_ACL:
        case FETCH_CREATE_SUBSCRIPTION:
        case FETCH_DELETE_SUBSCRIPTION:
            const i = nextState.findIndex((instance) => {
                return instance.id === action.instanceId;
            });

            if (i < 0) {
                return state;
            }

            nextState[i] = instanceReducer(nextState[i], action);
            break;
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
        case FETCH_DELETE_ORGANIZATION_INVITATION:
        case FETCH_DELETE_MEMBER:
            i = nextState.findIndex(member => {
                return (member.id === action.memberId) ||
                    (!member.id && member.email === action.invitation.email);
            });

            if (!i) {
                return state;
            }

            nextState.splice(i, 1);
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
        case FETCH_USERS_OF_INSTANCE:
        case FETCH_CREATE_SUBSCRIPTION:
        case FETCH_DELETE_SUBSCRIPTION:
            nextState.instances = instancesState(state.instances, action);
            break;
        case FETCH_UPDATE_ROLE_MEMBER:
        case FETCH_DELETE_MEMBER:
        case FETCH_DELETE_ORGANIZATION_INVITATION:
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
        case FETCH_UPDATE_STATUS_ORGANIZATION:
            const i = nextState.findIndex((org) => {
                return org.id === action.organization.id;
            });

            if (i < 0) {
                return state;
            }

            nextState[i] = Object.assign({}, nextState[i], action.organization);
            break;
        case FETCH_USER_ORGANIZATIONS:
        case FETCH_USER_ORGANIZATIONS_LAZY_MODE:
            nextState = action.organizations;
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
    switch (action.type) {
        case FETCH_USER_ORGANIZATIONS:
        case FETCH_USER_ORGANIZATIONS_LAZY_MODE:
        case FETCH_CREATE_ORGANIZATION:
        case FETCH_UPDATE_STATUS_ORGANIZATION:
            nextState.organizations = organizationsState(nextState.organizations, action);
            break;
        case FETCH_CREATE_SUBSCRIPTION:
        case FETCH_DELETE_SUBSCRIPTION:
        case FETCH_DELETE_MEMBER:
        case FETCH_UPDATE_ROLE_MEMBER:
        case FETCH_UPDATE_SERVICE_CONFIG:
        case FETCH_ADD_INSTANCE_TO_ORG:
        case FETCH_UPDATE_INSTANCE_STATUS:
        case FETCH_USERS_OF_INSTANCE:
        case FETCH_CREATE_ACL:
        case FETCH_DELETE_ACL:
        case FETCH_ORGANIZATION_WITH_ID:
        case FETCH_ORGANIZATION_INFO:
        case FETCH_UPDATE_ORGANIZATION:
        case FETCH_DELETE_ORGANIZATION_INVITATION:
        case FETCH_CREATE_ORGANIZATION_INVITATION:
            nextState.current = currentOrganizationState(state.current, action);
            break;
        default:
            return state;
    }

    return nextState;
}