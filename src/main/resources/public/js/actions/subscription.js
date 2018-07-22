import customFetch from '../util/custom-fetch';

export const FETCH_CREATE_SUBSCRIPTION = 'FETCH_CREATE_SUBSCRIPTION';
export const FETCH_DELETE_SUBSCRIPTION = 'FETCH_DELETE_SUBSCRIPTION';

/* Actions */
export const fetchCreateSubscriptionAction = (instanceId, sub) => {
    return {
        type: FETCH_CREATE_SUBSCRIPTION,
        service: {catalogEntry: {id: sub.service_id}},
        instanceId,
        sub
    };
};

export const fetchDeleteSubscriptionAction = (instanceId, sub) => {
    return {
        type: FETCH_DELETE_SUBSCRIPTION,
        service: {catalogEntry: {id: sub.service_id}},
        instanceId,
        sub
    };
};

/* Async methods */
export const fetchCreateSubscription = (instanceId, sub) => {
    return dispatch => {
        return customFetch(`/my/api/service/${sub.service_id}/subscription/${sub.user_id}`, {
            method: 'POST'
        }).then(newSub => {
            return dispatch(fetchCreateSubscriptionAction(instanceId, newSub));
        })
    };
};

export const fetchDeleteSubscription = (instanceId, sub) => {
    return dispatch => {
        return customFetch(`/my/api/service/${sub.service_id}/subscription/${sub.user_id}`, {
            method: 'DELETE'
        }).then(() => {
            return dispatch(fetchDeleteSubscriptionAction(instanceId, sub));
        })
    };
};