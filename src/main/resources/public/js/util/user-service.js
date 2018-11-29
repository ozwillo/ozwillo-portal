import customFetch from "./custom-fetch";

export const fetchUserInfos = async () => {
    return await customFetch('/api/user');
};