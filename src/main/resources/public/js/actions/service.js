import customFetch from '../util/custom-fetch';

export const FETCH_USERS_OF_SERVICE = 'FETCH_USERS_OF_SERVICE';

//Actions
const fetchUsersOfServiceAction = (serviceId, users) => {
    return {
        type: FETCH_USERS_OF_SERVICE,
        serviceId,
        users
    };
};

//Async methods
export const fetchUsersOfService = (service) => {
    return (dispatch) => {
        return customFetch(`/my/api/service/${service.catalogEntry.instance_id}/users`)
            .then(users => {
                return dispatch(fetchUsersOfServiceAction(service.catalogEntry.id, users));
            });
    };
};

