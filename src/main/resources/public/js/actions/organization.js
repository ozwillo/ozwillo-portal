import customFetch from '../util/custom-fetch';

export const FETCH_ORGANIZATIONS = 'FETCH_ORGANIZATIONS';
export const FETCH_USER_ORGANIZATIONS = 'FETCH_USER_ORGANIZATIONS';


const fetchOrganizationsAction = (organizations) => {
    return {
        type: FETCH_ORGANIZATIONS,
        organizations
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
            })
    };
};

export const fetchUserOrganizations = () => {
    return dispatch => {

        return customFetch('/my/api/network/organizations.json')
            .then((organizations) => {
                dispatch(fetchUserOrganizationsAction(organizations));
            })
    };
};
