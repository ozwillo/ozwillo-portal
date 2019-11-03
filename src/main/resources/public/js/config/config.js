export default {
    notificationsCountInterval: 600000,
    dashboardNotificationsInterval: 600000,
    sizeQueryBeforeFetch: 2,
    instanceStatus: {
        pending: 'PENDING',
        stopped: 'STOPPED',
        running: 'RUNNING'
    },
    instanceVisibility: {
        never: 'NEVER_VISIBLE',
        visible: 'VISIBLE',
        hidden: 'HIDDEN'
    },
    iconMaxSize: 20480, // octets
    appTypes: {
        application: 'application',
        service: 'service'
    },
    organizationStatus: {
        available: 'AVAILABLE',
        deleted: 'DELETED'
    }
};
