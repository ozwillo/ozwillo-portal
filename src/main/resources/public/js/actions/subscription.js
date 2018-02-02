import customFetch from '../util/custom-fetch';

export const fetchCreateSubscription = (userId, serviceId) => {
    return customFetch('/my/api/subscription', {
        method: 'POST',
        json: { userId, serviceId }
    })
};