import customFetch from "./custom-fetch";

export const fetchAppDetails = async (appType, appId) => {
    return await customFetch(`/api/store/details/${appType}/${appId}`);
};

export const fetchRateApp = async (appType, appId, rate) => {
    return await customFetch(`/api/store/rate/${appType}/${appId}`, {
        method: 'POST',
        json: {"rate": rate}
    });
};

export const fetchAvailableOrganizations = async (appType, appId) => {
    return await customFetch(`/api/store/organizations/${appType}/${appId}`);
};


export const buyApplication  = async (appId, appType, organizationSelected) =>{

    let request = {appId: appId, appType: appType};
    if (organizationSelected && organizationSelected.id) {
        request.organizationId = organizationSelected.id;
    }
    if (appType === 'service')
        return await customFetch(`/api/store/buy/${appType}/${appId}`, {
            method: 'POST'
        });
    else
        return await customFetch(`/api/store/buy/${appType}`, {
            method: 'POST',
            json: request
        });
};

export const addInstanceToOrg = async (organizationId, {id}) => {
    return await customFetch('/api/store/buy/application', {
        method: 'POST',
        json: {
            organizationId,
            appId: id
        }
    })
};

export const unavailableOrganizationToInstallAnApp = async (appId, appType) => {
    return await customFetch(`/api/store/${appType}/${appId}/organization/unavailable`)
}
