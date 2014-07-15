package org.oasis_eu.portal.config;

import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

/**
 * 
 * @author mkalam-alami
 *
 */
public class OasisLocaleResolver extends CookieLocaleResolver {

    public static final String LOCALE_COOKIE_NAME = "OASIS_LOCALE";
    
	@Autowired
    private UserInfoService userInfoService;
    
    public OasisLocaleResolver() {
        this.setCookieName(LOCALE_COOKIE_NAME);
	}

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		// Try to use profile information
		UserInfo currentUser = userInfoService.currentUser();
		if (currentUser != null) {
			return StringUtils.parseLocaleString(currentUser.getLocale());
		}
		// Otherwise use cookie
		else {
			return super.resolveLocale(request);
		}
	}
	
	public Locale getCookieLocale(HttpServletRequest request) {
		return super.resolveLocale(request);
	}
	
	@Override
	public void setDefaultLocale(Locale defaultLocale) {
		// Do not set a default locale - this will fall back to using the request's Accept header
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setDefaultTimeZone(TimeZone defaultTimeZone) {
		// Do not set a default locale - this will fall back to using the request's Accept header
		throw new UnsupportedOperationException();
	}
	
}
