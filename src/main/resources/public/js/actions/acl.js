import customFetch from '../util/custom-fetch';

export const FETCH_CREATE_ACL = 'FETCH_CREATE_ACL';
export const FETCH_DELETE_ACL = 'FETCH_DELETE_ACL';

const fetchCreateAclAction = (serviceId, user) => {
    return {
        type: FETCH_CREATE_ACL,
        user,
        serviceId
    };
};

const fetchDeleteAclAction = (serviceId, user) => {
    return {
        type: FETCH_DELETE_ACL,
        user,
        serviceId
    };
};

export const fetchCreateAcl = (user, service) => {
    return (dispatch) => {
        return customFetch('/my/api/acl', {
            method: 'POST',
            json: {
                user: user,
                instanceId: service.catalogEntry.instance_id
            }
        }).then(() => {
            return dispatch(fetchCreateAclAction(service.catalogEntry.id, user));
        });
    };
};

export const fetchDeleteAcl = (user, service) => {
    return (dispatch) => {
        return customFetch('/my/api/acl', {
            method: 'DELETE',
            json: {
                user: user,
                instanceId: service.catalogEntry.instance_id
            }
        }).then(() => {
            return dispatch(fetchDeleteAclAction(service.catalogEntry.id, user));
        });
    };
};