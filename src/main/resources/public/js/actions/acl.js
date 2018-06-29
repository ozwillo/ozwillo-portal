import customFetch from '../util/custom-fetch';

export const FETCH_CREATE_ACL = 'FETCH_CREATE_ACL';
export const FETCH_DELETE_ACL = 'FETCH_DELETE_ACL';

//Actions
const fetchCreateAclAction = (instanceId, user) => {
    return {
        type: FETCH_CREATE_ACL,
        user,
        instanceId
    };
};

const fetchDeleteAclAction = (instanceId, user) => {
    return {
        type: FETCH_DELETE_ACL,
        user,
        instanceId
    };
};


//Async methods
export const fetchCreateAcl = (user, instance) => {
    return (dispatch) => {
        return customFetch('/my/api/acl', {
            method: 'POST',
            json: {
                user: user,
                instanceId: instance.id
            }
        }).then(() => {
            return dispatch(fetchCreateAclAction(instance.id, user));
        });
    };
};

export const fetchDeleteAcl = (user, instance) => {
    return (dispatch) => {
        return customFetch('/my/api/acl', {
            method: 'DELETE',
            json: {
                user: user,
                instanceId: instance.id
            }
        }).then(() => {
            return dispatch(fetchDeleteAclAction(instance.id, user));
        });
    };
};