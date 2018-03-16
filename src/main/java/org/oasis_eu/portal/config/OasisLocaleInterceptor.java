package org.oasis_eu.portal.config;

import org.oasis_eu.portal.model.Languages;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Updates the cookie locale to match the user profile.
 *
 * @author mkalam-alami
 */
public class OasisLocaleInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OpenIdCAuthentication) {

            // nop - now we should have a correct locale in the session

        } else {
            // a bit hacky, basically if we're not logged in we look for the first path element to tell us the locale
            String path = request.getServletPath();
            if (path.contains("/")) {
                String[] split = path.split("/");
                if (split.length > 0) {
                    Languages foundLanguage = Languages.getByLanguageTag(split[1]);
                    if (foundLanguage != null) {
                        RequestContextUtils.getLocaleResolver(request).setLocale(request, response, foundLanguage.getLocale());
                    }
                }
            }
        }
        return true;
    }

}
