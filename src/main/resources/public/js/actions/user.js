export const FETCH_USER_INFO = "FETCH_USER_INFO";

export const fetchUserInfoAction = (userInfo) => {
    return {
        type: FETCH_USER_INFO,
        userInfo
    }
}