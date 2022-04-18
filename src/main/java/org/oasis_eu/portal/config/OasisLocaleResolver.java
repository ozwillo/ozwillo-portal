package org.oasis_eu.portal.config;

import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * @author mkalam-alami
 */
public class OasisLocaleResolver extends SessionLocaleResolver {
    private static final Logger logger = LoggerFactory.getLogger(OasisLocaleResolver.class);

//	public static final String LOCALE_COOKIE_NAME = "OASIS_LOCALE";

    @Autowired
    private UserInfoService userInfoService;

    public OasisLocaleResolver() {

    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // Try to use profile information
        UserInfo currentUser = userInfoService.currentUser();
        if (currentUser != null && StringUtils.hasLength(currentUser.getLocale())) {
            Locale userLocale = Locale.forLanguageTag(currentUser.getLocale());
            if (!userLocale.getLanguage().isEmpty()) {
                return userLocale; // ex. "en-GB fr" => en
                // and NOT Spring's StringUtils.parseLocaleString()
                // which parses only ONE locale AND with '_' separator and optional variant ex. "en_GB_GB"
            } // else ex. "und" meaning undefined for Kernel
        }
        // Otherwise use parameter
        if (request.getParameter("ui_locales") != null) {
            try {
                Locale l = Locale.forLanguageTag(request.getParameter("ui_locales"));
                if (l != null) {
                    return l;
                }
            } catch (Exception e) {
                logger.error("Cannot interpret locale from ui_locales parameter: {}", request.getParameter("ui_locales"));
                logger.info("Exception:", e);
            }
        }
        return super.resolveLocale(request);
    }


}
