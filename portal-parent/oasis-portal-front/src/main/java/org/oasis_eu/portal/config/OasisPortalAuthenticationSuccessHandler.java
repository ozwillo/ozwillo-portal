package org.oasis_eu.portal.config;

import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: schambon
 * Date: 9/26/14
 */
public class OasisPortalAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    AuthenticationSuccessHandler defaultHandler = new SavedRequestAwareAuthenticationSuccessHandler();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        Object appStoreStatus = request.getSession().getAttribute("APP_STORE");
        if (appStoreStatus != null) {

            OpenIdCAuthentication auth = (OpenIdCAuthentication) authentication;

            AppStoreNavigationStatus status = (AppStoreNavigationStatus) appStoreStatus;
            String redirect;
            if (status.hasApp()) {
                redirect = "/" + getLocale(auth.getUserInfo(), request) + "/store/application/" + status.getAppId() + "/" + status.getAppType() + "?fromAuth=true";
            } else {
                redirect = "/" + getLocale(auth.getUserInfo(), request) + "/store";
            }

            response.sendRedirect(redirect);

        } else {
            defaultHandler.onAuthenticationSuccess(request, response, authentication);
        }

    }

    private String getLocale(UserInfo userInfo, HttpServletRequest request) {
        Languages found = Languages.getByLanguageTag(userInfo.getLocale());
        if (found != null) {
            return found.getLanguage();
        } else return RequestContextUtils.getLocale(request).getLanguage();
    }
}
