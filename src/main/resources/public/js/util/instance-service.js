import customFetch from "./custom-fetch";

export  default class InstanceService {


    fetchInstanceServices = async (instanceId, withSubscriptions) => {
        return await customFetch(`/my/api/instance/${instanceId}/services?withSubscriptions=${withSubscriptions}`);
    };

    fetchUsersOfInstance = async (instanceId) => {
        return await customFetch(`/my/api/instance/${instanceId}/users`);
    };

    fetchInstancesOfUserForOrganization = async (organizationId, userId) => {
        return await customFetch(`/my/api/instance/organization/${organizationId}/user/${userId}`);
    };

    fetchInstancesOfOrganization = async (organizationId) => {
        return await customFetch(`/my/api/instance/organization/${organizationId}`);
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

    updateInstanceStatus = async (instance, status) => {
        return await customFetch(`/my/api/instance/${instance.id}/status`, {
            method: 'POST',
            json: {applicationInstance: {id: instance.id, status}}
        })
    }


}
