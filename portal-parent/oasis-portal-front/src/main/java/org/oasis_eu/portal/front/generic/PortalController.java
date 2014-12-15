package org.oasis_eu.portal.front.generic;

import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapEntry;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.portal.services.NameDefaults;
import org.oasis_eu.spring.kernel.exception.AuthenticationRequiredException;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.oasis_eu.spring.kernel.security.OpenIdCService;
import org.oasis_eu.spring.kernel.security.RefreshTokenNeedException;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * User: schambon Date: 6/11/14
 */
abstract public class PortalController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserInfoService userInfoService;

    @Autowired
    private OpenIdCService openIdCService;

	@Autowired
	private HttpServletRequest request;

    @Autowired
    private NameDefaults nameDefaults;

    @Autowired
    private MyNavigationService navigationService;


    @Value("${web.home}")
    private String webHome;

    @Value("${application.devmode:false}")
    private boolean devmode;

    @Value("${application.production:false}")
    private boolean production;


    @ModelAttribute("webHome")
    public String getWebHome() {
        return webHome;
    }

    @ModelAttribute("devmode")
    public boolean getDevMode() {
        return devmode;
    }

    @ModelAttribute("production")
    public boolean getProduction() {
        return production;
    }

	@ModelAttribute("languages")
	public Languages[] languages() {
		return Languages.values();
	}

	@ModelAttribute("currentLanguage")
	public Languages currentLanguage() {
		
		// user's locale already normalized by nameDefaults.complete in user(),
		// however user may be null if not logged (or logout filter)
		if(user()!=null && user().getLocale()!=null) {
			return Languages.getByLocale(Locale.forLanguageTag(user().getLocale()), Languages.ENGLISH);
		}
		return Languages.getByLocale(RequestContextUtils.getLocale(request));
	}

	@ModelAttribute("user")
	public UserInfo user() {
        UserInfo userInfo = userInfoService.currentUser();
        if (userInfo == null) return null;

        return nameDefaults.complete(userInfo);
	}

    @ModelAttribute("sitemap")
    public List<List<SiteMapEntry>> siteMap() {
        List<SiteMapEntry> siteMap = navigationService.getSiteMap();

        if (siteMap == null || siteMap.isEmpty()) {
            return Collections.emptyList();
        }

        int colLength = 4;
        List<List<SiteMapEntry>> result = new ArrayList<>();
        List<SiteMapEntry> current = new ArrayList<>(colLength);
        for (SiteMapEntry entry : siteMap) {
            current.add(entry);

            if (current.size() == colLength) {
                result.add(current);
                current = new ArrayList<>(colLength);
            }
        }
        if (current.size() != 0) {
            result.add(current);
        }


        return result;
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    public void handleAuthRequired(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("/my");

    }

    @ExceptionHandler(value = ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleForbidden() {

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleErrors(Exception e) {

        if (e instanceof RefreshTokenNeedException) {
            throw (RefreshTokenNeedException) e;
        }

        logger.error("Cannot process request: {}", new Object[]{e.getMessage(), e});

        return "error";
    }

    @ModelAttribute("isAppstore")
    public boolean isAppstore() {
        return false;
    }


    protected boolean requiresLogout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication instanceof OpenIdCAuthentication) {
            if (openIdCService.getUserInfo(((OpenIdCAuthentication) authentication).getAccessToken()) == null) {
                return true;
            }
        }
        return false;
    }

}
