import customFetch from "./custom-fetch";


export default class UserService{

    fetchUserInfos = async () => {
        return await customFetch('/api/user');
    };
}
