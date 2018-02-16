import customFetch, { urlBuilder } from '../util/custom-fetch';

export const FETCH_ORGANIZATION_WITH_ID = 'FETCH_ORGANIZATION_WITH_ID';
export const FETCH_USER_ORGANIZATIONS = 'FETCH_USER_ORGANIZATIONS';
export const FETCH_CREATE_ORGANIZATION = 'FETCH_CREATE_ORGANIZATION';
export const FETCH_UPDATE_ORGANIZATION = 'FETCH_UPDATE_ORGANIZATION';
export const FETCH_ORGANIZATION_INFO = 'FETCH_ORGANIZATION_INFO';

// Actions
const fetchOrganizationWithIdAction = (organization) => {
    return {
        type: FETCH_ORGANIZATION_WITH_ID,
        organization
    };
};

const fetchUserOrganizationsAction = (organizations) => {
    return {
        type: FETCH_USER_ORGANIZATIONS,
        organizations
    };
};

const fetchCreateOrganizationAction = (organization) => {
    return {
        type: FETCH_CREATE_ORGANIZATION,
        organization
    };
};

const fetchUpdateOrganizationAction = (organization) => {
    return {
        type: FETCH_UPDATE_ORGANIZATION,
        organization
    };
};

const fetchOrganizationInfoAction = (info) => {
    return {
        type: FETCH_ORGANIZATION_INFO,
        info
    };
};

// Async methods
export const fetchOrganizationWithId = (id) => {
    return dispatch => {
        return customFetch(`/my/api/organization/${id}`)
            .then((organization) => {
                dispatch(fetchOrganizationWithIdAction(organization));
            });
    };
};

export const fetchUserOrganizations = () => {
    return dispatch => {
        return customFetch('/my/api/organization.json')
            .then((organizations) => {
                dispatch(fetchUserOrganizationsAction(organizations));
            });
    };
};

export const fetchCreateOrganization = (organization) => {
    return dispatch => {
        return customFetch('/my/api/organization', {
            method: 'POST',
            json: organization
        }).then((org) => {
            dispatch(fetchCreateOrganizationAction(org));
        });
    };
};

export const fetchUpdateOrganization = (info) => {
    return dispatch => {
        return customFetch('/my/api/organization', {
            method: 'PUT',
            json: info
        }).then((org) => {
            org.info = info
            dispatch(fetchUpdateOrganizationAction(org));
        });
    };
};

export const fetchOrganizationInfo = (dcId) => {
    return dispatch => {
        return customFetch(urlBuilder('/my/api/organization/info', { dcId }))
            .then((info) => {
                return dispatch(fetchOrganizationInfoAction(info));
            });
    };
};
