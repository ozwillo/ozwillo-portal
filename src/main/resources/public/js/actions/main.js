import {setLanguage, setTranslations} from "redux-i18n";
import customFetch from "../util/custom-fetch";

import { fetchConfigAction } from "./config";
import { fetchUserInfoAction} from "./user";

export const fetchConfigAndUserInfo = () => {
    return (dispatch) => {
        return customFetch('/my/api/configAndUserInfo.json')
            .then((res) => {
                // Language
                dispatch(setLanguage(res.language));
                dispatch(setTranslations(res.i18n));

                //Config
                dispatch(fetchConfigAction({
                    siteMapFooter: res.siteMapFooter,
                    language: res.language,
                    kernelEndPoint: res.kernelEndPoint,
                    accountEndPoint: res.accountEndPoint,
                    devMode: res.devMode
                }));
                dispatch(fetchUserInfoAction(res.userInfo));

                return res;
            })
    };
};