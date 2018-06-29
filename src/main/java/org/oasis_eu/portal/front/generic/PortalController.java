package org.oasis_eu.portal.front.generic;

import org.oasis_eu.portal.model.OasisLocales;
import org.oasis_eu.portal.model.Languages;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapEntry;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapMenuSet;
import org.oasis_eu.portal.services.LocaleService;
import org.oasis_eu.portal.services.MyNavigationService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    private MyNavigationService navigationService;

    @Autowired
    private LocaleService localeService;

    @Value("${web.home}")
    private String webHome;

    @Value("${opendata.url}")
    private String opendataUrl;

    @Value("${application.devmode:false}")
    private boolean devmode;

    @Value("${application.notificationsEnabled:true}")
    private boolean notificationsEnabled;

    @Value("${application.production:false}")
    private boolean production;


    @ModelAttribute("webHome")
    public String getWebHome() {
        return webHome;
    }

    @ModelAttribute("opendataUrl")
    public String getOpendataUrl() {
        return opendataUrl;
    }

    @ModelAttribute("devmode")
    public boolean getDevMode() {
        return devmode;
    }

    @ModelAttribute("notificationsEnabled")
    public boolean getNotificationsEnabled() {
        return notificationsEnabled;
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
        return Languages.getByLocale(currentLocale(), Languages.ENGLISH); // english if user locale unknown but not null
    }

    private Locale currentLocale() {
        if (user() != null && user().getLocale() != null) { // if user logged and locale set
            return localeService.getBestLocale(Locale.forLanguageTag(user().getLocale()), OasisLocales.locales()); // null if unknown but not null
        }
        // else if not logged (or logout filter), or logged user locale not set
        return RequestContextUtils.getLocale(request);
    }

    /**
     * else enum harder to use in js & react.js ; also in the CURRENT locale
     */
    @ModelAttribute("localeLanguages")
    public List<String> localeLanguages() {
        return Arrays.asList(Languages.values()).stream()
            .map(l -> l.getLocale().getLanguage()).collect(Collectors.toList());
    }

    /**
     * else enum harder to use in js & react.js
     */
    @ModelAttribute("currentLocaleLanguage")
    public String currentLocaleLanguage() {
        return currentLanguage().getLocale().getLanguage();
    }

    @ModelAttribute("user")
    public UserInfo user() {
        return userInfoService.currentUser(); // #186 NOT nameDefaults.complete(userInfo) which overrides ex. "en-GB fr" Kernel locale
    }

    @ModelAttribute("sitemapFooter")
    public Map<Integer, List<SiteMapEntry>> siteMapFooter() {
        return navigationService.getSiteMapFooter();
    }

    @ModelAttribute("sitemapHeader")
    public SiteMapMenuSet siteMapHeader() {
        SiteMapMenuSet siteMap = navigationService.getSiteMapHeader();

        if (siteMap == null) {
            return new SiteMapMenuSet();
        }

        return siteMap;
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

            OpenIdCAuthentication openIdCAuthentication = (OpenIdCAuthentication) authentication;

            return openIdCService.getUserInfo(openIdCAuthentication) == null;
        }
        return false;
    }

}
