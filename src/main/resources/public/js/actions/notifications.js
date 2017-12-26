import customFetch, {urlBuilder} from '../util/custom-fetch';

export const FETCH_NOTIFICATIONS_COUNT = 'FETCH_NOTIFICATIONS_COUNT';
export const DELETE_NOTIFICATION = 'DELETE_NOTIFICATION';
export const FETCH_NOTIFICATIONS = 'FETCH_NOTIFICATIONS';

// Actions
const fetchNotificationsCountAction = (message, count) => {
    return {
        type: FETCH_NOTIFICATIONS_COUNT,
        message,
        count
    };
};

const fetchNotificationsAction = (notifications, apps) => {
    return {
        type: FETCH_NOTIFICATIONS,
        notifications,
        apps
    };
};

const deleteNotificationAction = (id) => {
    return {
        type: DELETE_NOTIFICATION,
        id
    };
};

// Methods
export const fetchNotificationsCount = () => {
    return (dispatch) => {
        return customFetch('/my/api/notifications/summary').then((res) => {
            dispatch(fetchNotificationsCountAction(res.notificationsMessage, res.notificationsCount));
        });
    };
};

export const fetchNotifications = (status) => {
    return (dispatch) => {
        return customFetch(urlBuilder('/my/api/notifications', {status}))
            .then((res) => {
                dispatch(fetchNotificationsAction(res.notifications, res.apps));
            });
    };
};

export const deleteNotification = (id) => {
    return (dispatch) => {
        return customFetch(`/my/api/notifications/${id}`, {
            method: 'DELETE'
        }).then(() => {
            dispatch(deleteNotificationAction(id));
        });
    };
};