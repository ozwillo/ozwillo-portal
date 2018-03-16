import customFetch from '../util/custom-fetch';

export const FETCH_USERS_OF_INSTANCE = 'FETCH_USERS_OF_INSTANCE';
export const FETCH_UPDATE_INSTANCE_STATUS = 'FETCH_UPDATE_INSTANCE_STATUS';
export const FETCH_UPDATE_SERVICE_CONFIG = 'FETCH_UPDATE_SERVICE_CONFIG';

//Actions
const fetchUsersOfInstanceAction = (instanceId, users) => {
    return {
        type: FETCH_USERS_OF_INSTANCE,
        instanceId,
        users
    };
};

const fetchUpdateInstanceStatusAction = (instance) => {
    return {
        type: FETCH_UPDATE_INSTANCE_STATUS,
        instanceId: instance.id,
        instance
    };
};

const fetchUpdateServiceConfigAction = (instanceId, service) => {
    return {
        type: FETCH_UPDATE_SERVICE_CONFIG,
        instanceId,
        service
    };
};

//Async methods
export const fetchUsersOfInstance = (instance) => {
    return (dispatch) => {
        return customFetch(`/my/api/instance/${instance.id}/users`)
            .then(users => {
                return dispatch(fetchUsersOfInstanceAction(instance.id, users));
            });
    };
};

export const fetchUpdateInstanceStatus = ({applicationInstance}, status) => {
    return (dispatch) => {
        return customFetch(`/my/api/instance/${applicationInstance.id}/status`, {
            method: 'POST',
            json: {applicationInstance: {id: applicationInstance.id, status}}
        }).then(({id, applicationInstance, deletion_planned}) => {
            return dispatch(fetchUpdateInstanceStatusAction({id, applicationInstance, deletion_planned}));
        });
    };
};


export const fetchUpdateServiceConfig = (instanceId, catalogEntry) => {
    return (dispatch) => {
        return customFetch(`/my/api/service/${catalogEntry.id}`, {
            method: 'PUT',
            json: catalogEntry
        }).then((service) => {
            return dispatch(fetchUpdateServiceConfigAction(instanceId, service));
        });
    };
};




