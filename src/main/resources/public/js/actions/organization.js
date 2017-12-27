import customFetch, { urlBuilder } from '../util/custom-fetch';

export const FETCH_ORGANIZATIONS = 'FETCH_ORGANIZATIONS';
export const FETCH_ORGANIZATION_WITH_ID = 'FETCH_ORGANIZATION_WITH_ID';
export const FETCH_USER_ORGANIZATIONS = 'FETCH_USER_ORGANIZATIONS';


const fetchOrganizationsAction = (organizations) => {
    return {
        type: FETCH_ORGANIZATIONS,
        organizations
    };
};

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

export const fetchOrganizations = () => {
    return dispatch => {
        return customFetch('/my/api/network/search-organizations.json')
            .then((organizations) => {
                dispatch(fetchOrganizationsAction(organizations));
            });
    };
};

export const fetchOrganizationWithId = (id) => {
    return dispatch => {
        return customFetch(urlBuilder(`/my/api/network/organization.json`, { dc_id: id }))
            .then((organization) => {
                dispatch(fetchOrganizationWithIdAction(organization));
            });
    };
};

export const fetchUserOrganizations = () => {
    return dispatch => {
        return customFetch('/my/api/network/organizations.json')
            .then((organizations) => {
                dispatch(fetchUserOrganizationsAction(organizations));
            });
    };
};
