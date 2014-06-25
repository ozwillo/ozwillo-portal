package org.oasis_eu.portal.front.generic;

import org.oasis_eu.portal.services.UserInfoHelper;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * User: schambon
 * Date: 6/11/14
 */
abstract public class PortalController {

    @Autowired
    private UserInfoHelper userInfoHelper;

    @ModelAttribute("languages")
    public Languages[] languages() {
        return Languages.values();
    }


    @ModelAttribute("currentLanguage")
    public Languages currentLanguage(HttpServletRequest request) {
        Locale current = RequestContextUtils.getLocale(request);
        for (Languages l : Languages.values()) {
            if (l.getLanguage().equals(current.getLanguage())) {
                return l;
            }
        }
        return Languages.ENGLISH;
    }

    @ModelAttribute("user")
    public UserInfo user() {
        return userInfoHelper.currentUser();
    }

}
