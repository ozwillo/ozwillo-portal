import customFetch from "../util/custom-fetch";

export const FETCH_USER_INFO = "FETCH_USER_INFO";

export const fetchUserInfoAction = (userInfo) => {
    return {
        type: FETCH_USER_INFO,
        userInfo
    }
};

export const fetchUserInfo = () => {
    return (dispatch) => {
        return customFetch('/api/user.json')
            .then((userInfo) => {
                return dispatch(fetchUserInfoAction(userInfo));
            });
    };
};