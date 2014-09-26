package org.oasis_eu.portal.config;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Updates the cookie locale to match the user profile.
 * 
 * @author mkalam-alami
 *
 */
public class OasisLocaleInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OpenIdCAuthentication) {
        	
			UserInfo currentUser = ((OpenIdCAuthentication) authentication).getUserInfo();
			if (currentUser != null && !StringUtils.isEmpty(currentUser.getLocale())) {
				String userLocale = currentUser.getLocale();
				LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
				if (localeResolver instanceof OasisLocaleResolver) {
					OasisLocaleResolver olr = (OasisLocaleResolver) localeResolver;
					if (!userLocale.equals(olr.getCookieLocale(request).toString())) {
						localeResolver.setLocale(request, response, StringUtils.parseLocaleString(userLocale));
					}
				}
			}

        } else {
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
