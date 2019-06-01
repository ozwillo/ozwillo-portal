import customFetch, {urlBuilder} from '../util/custom-fetch';

export const FETCH_NOTIFICATIONS_COUNT = 'FETCH_NOTIFICATIONS_COUNT';

// Actions
const fetchNotificationsCountAction = (count) => {
    return {
        type: FETCH_NOTIFICATIONS_COUNT,
        count
    };
};

// Methods
export const fetchNotificationsCount = () => {
    return (dispatch) => {
        return customFetch('/my/api/notifications/summary').then((res) => {
            dispatch(fetchNotificationsCountAction(res));
        });
    };
};
