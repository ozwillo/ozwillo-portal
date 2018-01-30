import customFetch from '../util/custom-fetch';

export const FETCH_CREATE_ACL = 'FETCH_CREATE_ACL';

const fetchCreateAclAction = (serviceId, user) => {
    return {
        type: FETCH_CREATE_ACL,
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