package org.oasis_eu.portal.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapEntry;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapMenuSet;
import org.oasis_eu.portal.front.generic.i18nMessages;
import org.oasis_eu.portal.model.Languages;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class OzwilloController {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MyNavigationService navigationService;

    @Autowired
    private UserInfoService userInfoService;

    @Value("${kernel.base_uri}")
    private String kernelEndPoint;

    @Value("${kernel.account_uri}")
    private String accountEndPoint;

    @Value("${opendata.url}")
    private String opendatEndPoint;

    @Value("${application.devmode:false}")
    private boolean devMode;

    @Value("${application.notificationsEnabled:true}")
    private boolean notificationsEnabled;

    @GetMapping("/user")
    public UserInfo getCurrentUser() {
        return userInfoService.currentUser();
    }

    @GetMapping("/config")
    public Config getConfig(HttpServletRequest request) throws JsonProcessingException {
        // i18n
        Locale locale = RequestContextUtils.getLocale(request);
        Map<String, Map<String, String>> i18n = new HashMap<>();
        i18n.put(locale.getLanguage(), i18nMessages.getI18n_all(locale, messageSource));

        List<String> languages = Arrays.asList(Languages.values()).stream()
                .map(l -> l.getLocale().getLanguage()).collect(Collectors.toList());

        //Site map
        Map<Integer, List<SiteMapEntry>> siteMapFooter = navigationService.getSiteMapFooter();
        SiteMapMenuSet siteMapHeader = navigationService.getSiteMapHeader();

        //MyConfig object
        Config config = new Config();
        config.language = locale.getLanguage();
        config.languages = languages;
        config.i18n = i18n;
        config.siteMapFooter = siteMapFooter;
        config.siteMapHeader = siteMapHeader;
        config.kernelEndPoint = kernelEndPoint;
        config.accountEndPoint = accountEndPoint;
        config.opendatEndPoint = opendatEndPoint;
        config.devMode = devMode;
        config.notificationsEnabled = notificationsEnabled;

        return config;
    }

    @GetMapping("/config/language/{lang}")
    public Config getLanguage(@PathVariable String lang) throws JsonProcessingException {
        Map<String, Map<String, String>> i18n = new HashMap<>();
        i18n.put(lang, i18nMessages.getI18n_all(new Locale(lang) , messageSource));

        //Site map
        Map<Integer, List<SiteMapEntry>> siteMapFooter = navigationService.getSiteMapFooter(lang);
        SiteMapMenuSet siteMapHeader = navigationService.getSiteMapHeader(lang);

        Config config = new Config();
        config.siteMapFooter = siteMapFooter;
        config.siteMapHeader = siteMapHeader;
        config.i18n = i18n;
        return config;
    }

    public static class Config {
        @JsonProperty
        String language;

        @JsonProperty
        List<String> languages;

        @JsonProperty
        Map<String, Map<String, String>> i18n;

        @JsonProperty
        Map<Integer, List<SiteMapEntry>> siteMapFooter;

        @JsonProperty
        SiteMapMenuSet siteMapHeader;

        @JsonProperty
        String kernelEndPoint;

        @JsonProperty
        String accountEndPoint;

        @JsonProperty
        String opendatEndPoint;

        @JsonProperty
        boolean devMode;

        @JsonProperty
        boolean notificationsEnabled;
    }
}
