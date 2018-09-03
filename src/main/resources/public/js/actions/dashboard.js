import customFetch from '../util/custom-fetch';

export const fetchDashboards = () => {
    return customFetch('/my/api/dashboard/dashboards');
};

export const fetchCreateDashboard = (name) => {
    return customFetch('/my/api/dashboard/dashboards', {
        method: 'POST',
        json: {name}
    });
};

export const fetchRenameDashboard = (dashId, name) => {
    return customFetch(`/my/api/dashboard/dashboard/${dashId}`, {
        method: 'PUT',
        json: {name}
    });
};

export const fetchDeleteDashboard = (dashId) => {
    return customFetch(`/my/api/dashboard/dashboard/${dashId}`,
        {
            method: 'DELETE',
        });
};

export const fetchApps = (dashId) => {
    return customFetch(`/my/api/dashboard/apps${(dashId && `/${dashId}`) || ''}`);
};

export const fetchPendingApps = () => {
    return customFetch(`/my/api/dashboard/pending-apps`);
};

export const fetchReorderApps = (dashId, apps) => {
    return customFetch(`/my/api/dashboard/apps/${dashId}`,
        {
            method: 'PUT',
            json: {apps}
        });
};


export const fetchDeleteApp = (appId) => {
    //TODO: replace url by '/my/api/dashboard/apps/{id}' (delete request)
    return customFetch(`/my/api/dashboard/apps/remove/${appId}`,
        {
            method: 'DELETE',
        });
};

export const fetchDeletePendingApp = (appId) => {
    return customFetch(`/my/api/dashboard/pending-apps/${appId}`,
        {
            method: 'DELETE',
        });
};

export const moveToDash = (appId, dashId) => {
    return customFetch(`/my/api/dashboard/apps/move/${appId}/to/${dashId}`,
        {
            method: 'POST',
        });
};