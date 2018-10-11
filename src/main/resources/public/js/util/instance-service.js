import customFetch from "./custom-fetch";

export  default class InstanceService {


    fetchInstanceServices = async (instanceId, withSubscriptions) => {
        return await customFetch(`/my/api/instance/${instanceId}/services?withSubscriptions=${withSubscriptions}`);
    };

    fetchUsersOfInstance = async (instanceId) => {
        return await customFetch(`/my/api/instance/${instanceId}/users`);
    };

    fetchCreateSubscription = async (serviceId, userId) => {
        return await customFetch(`/my/api/service/${serviceId}/subscription/${userId}`, {
            method: 'POST'
        })
    };

    fetchDeleteSubscription = async (serviceId, userId) => {
        return await customFetch(`/my/api/service/${serviceId}/subscription/${userId}`, {
            method: 'DELETE'
        })
    };

    fetchCreateAcl = async (user, instance) => {
        return await customFetch(`/my/api/instance/${instance.id}/acl`, {
            method: 'POST',
            json: {
                userId: user.id,
                email: user.email
            }
        });
    };

    fetchDeleteAcl = async (user, instance) => {
        return await customFetch(`/my/api/instance/${instance.id}/acl`, {
            method: 'DELETE',
            json: {
                userId: user.id,
                email: user.email
            }
        });
    }
}