import customFetch, {urlBuilder} from '../util/custom-fetch';

export const FETCH_ORGANIZATION_WITH_ID = 'FETCH_ORGANIZATION_WITH_ID';
export const FETCH_USER_ORGANIZATIONS = 'FETCH_USER_ORGANIZATIONS';
export const FETCH_UPDATE_ORGANIZATION = 'FETCH_UPDATE_ORGANIZATION';
export const FETCH_ORGANIZATION_INFO = 'FETCH_ORGANIZATION_INFO';
export const FETCH_ORGANIZATION_MEMBERS = 'FETCH_ORGANIZATION_MEMBERS';

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

const fetchOrganizationMembersAction = (members) => {
    return {
        type: FETCH_ORGANIZATION_MEMBERS,
        members
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
        return customFetch('/my/api/organization')
            .then((organizations) => {
                dispatch(fetchUserOrganizationsAction(organizations));
            });
    };
};

export const fetchUpdateOrganization = (info) => {
    return dispatch => {
        //Transform empty fields to null
        Object.keys(info).forEach(key => {
            info[key] = info[key] || null;
        });

        return customFetch('/my/api/organization', {
            method: 'PUT',
            json: info
        }).then((org) => {
            //update version
            info.version = `${parseInt(info.version, 10) + 1}`;

            //update organization
            org.info = info;
            dispatch(fetchUpdateOrganizationAction(org));
        });
    };
};

export const fetchOrganizationInfo = (dcId) => {
    return dispatch => {
        return customFetch(urlBuilder('/my/api/organization/info', {dcId}))
            .then((info) => {
                return dispatch(fetchOrganizationInfoAction(info));
            });
    };
};


export const fetchOrganizationMembers = (organizationId) => {
    return (dispatch => {
        return customFetch(`/my/api/organization/${organizationId}/members`)
            .then(members => {
                return dispatch(fetchOrganizationMembersAction(members));
            })
    })
};
