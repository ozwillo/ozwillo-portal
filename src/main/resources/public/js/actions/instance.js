import customFetch from '../util/custom-fetch';

export const FETCH_USERS_OF_INSTANCE = 'FETCH_USERS_OF_INSTANCE';

//Actions
const fetchUsersOfInstanceAction = (instanceId, users) => {
    return {
        type: FETCH_USERS_OF_INSTANCE,
        instanceId,
        users
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

