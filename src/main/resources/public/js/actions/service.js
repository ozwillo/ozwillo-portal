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

export const fetchUsersOfService = (serviceId) => {
    return (dispatch) => {
        return customFetch(`/my/api/service/${serviceId}/users`)
            .then(users => {
                return dispatch(fetchUsersOfServiceAction(serviceId, users));
            });
    };
};