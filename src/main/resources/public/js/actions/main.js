import {setLanguage, setTranslations} from "redux-i18n";
import customFetch from "../util/custom-fetch";

import { fetchSiteMapFooterAction } from "./config";
import { fetchUserInfoAction} from "./user";

export const fetchConfigAndUserInfo = () => {
    return (dispatch) => {
        return customFetch('/my/api/configAndUserInfo.json')
            .then((res) => {
                // Language
                dispatch(setLanguage(res.language));
                dispatch(setTranslations(res.i18n));

                //Site map footer
                dispatch(fetchSiteMapFooterAction(res.siteMapFooter));

                //UserInfo
                dispatch(fetchUserInfoAction(res.userInfo));

                return res;
            })
    };
};