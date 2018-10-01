import customFetch from "./custom-fetch";

export const fetchInstanceServicesSubscriptions = async (instanceId) => {
    return await customFetch(`/my/api/instance/${instanceId}/services/subscription`);
};

export const fetchUsersOfInstance = async (instanceId) => {
    return await customFetch(`/my/api/instance/${instanceId}/users`);
};

export const  fetchCreateSubscription = async (serviceId, userId) => {
    return await customFetch(`/my/api/service/${serviceId}/subscription/${userId}`, {
        method: 'POST'
    })
};

export const fetchDeleteSubscription = async (serviceId, userId) => {
    return await customFetch(`/my/api/service/${serviceId}/subscription/${userId}`, {
        method: 'DELETE'
    })
};