import customFetch, {urlBuilder} from '../util/custom-fetch';

export const FETCH_ORGANIZATION_WITH_ID = 'FETCH_ORGANIZATION_WITH_ID';
export const FETCH_USER_ORGANIZATIONS = 'FETCH_USER_ORGANIZATIONS';
export const FETCH_USER_ORGANIZATIONS_LAZY_MODE = 'FETCH_USER_ORGANIZATIONS_LAZY_MODE';
export const FETCH_CREATE_ORGANIZATION = 'FETCH_CREATE_ORGANIZATION';
export const FETCH_UPDATE_ORGANIZATION = 'FETCH_UPDATE_ORGANIZATION';
export const FETCH_ORGANIZATION_INFO = 'FETCH_ORGANIZATION_INFO';
export const FETCH_UPDATE_STATUS_ORGANIZATION = 'FETCH_UPDATE_STATUS_ORGANIZATION';

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

const fetchUserOrganizationsLazyModeAction = (organizations) => {
    return {
        type: FETCH_USER_ORGANIZATIONS_LAZY_MODE,
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

const fetchUpdateStatusOrganizationAction = (organization) => {
    return {
        type: FETCH_UPDATE_STATUS_ORGANIZATION,
        organization
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

export const fetchUserOrganizationsLazyMode = () => {
    return (dispatch, getState) => {

        const organizations = getState().organization.organizations;
        if(organizations.length) {
            return Promise.resolve(organizations);
        }

        return customFetch('/my/api/organization/lazy.json')
            .then((organizations) => {
                dispatch(fetchUserOrganizationsLazyModeAction(organizations));
            });
    };
};

export const fetchCreateOrganization = (info) => {
    return dispatch => {
        //Transform empty fields to null
        Object.keys(info).forEach(key => {
            info[key] = info[key] || null;
        });

        return customFetch('/my/api/organization', {
            method: 'POST',
            json: {...info}
        }).then((org) => {
            dispatch(fetchCreateOrganizationAction(org));
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

export const fetchUpdateStatusOrganization = (organization) => {
    return dispatch => {
        return customFetch(`/my/api/organization/${organization.id}/status`, {
            method: 'PUT',
            json: organization
        }).then(({id, status}) => {
            return dispatch(fetchUpdateStatusOrganizationAction({id, status}));
        });
    };
};
