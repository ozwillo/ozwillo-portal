package org.oasis_eu.portal.front.my;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapEntry;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapMenuSet;
import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.portal.front.generic.i18nMessages;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("my/api")
public class MyOzwilloAJAXServices extends BaseAJAXServices {

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

    @Value("${application.devmode:false}")
    private boolean devMode;

    @GetMapping("/configAndUserInfo")
    public ConfigAndUserInfo getConfig(HttpServletRequest request) throws JsonProcessingException {
        // trad
        Locale locale = RequestContextUtils.getLocale(request);
        Map<String, Map<String, String>> i18n = new HashMap<>();
        i18n.put(locale.getLanguage(), i18nMessages.getI18n_all(locale, messageSource));

        //Site map footer
        Map<Integer, List<SiteMapEntry>> siteMapFooter = navigationService.getSiteMapFooter();

        //ConfigAndUserInfo object
        ConfigAndUserInfo configAndUserInfo = new ConfigAndUserInfo();
        configAndUserInfo.language = locale.getLanguage();
        configAndUserInfo.i18n = i18n;
        configAndUserInfo.siteMapFooter = siteMapFooter;
        configAndUserInfo.userInfo = userInfoService.currentUser();
        configAndUserInfo.kernelEndPoint = kernelEndPoint;
        configAndUserInfo.accountEndPoint = accountEndPoint;
        configAndUserInfo.devMode = devMode;

        return configAndUserInfo;
    }

    public static class ConfigAndUserInfo {
        @JsonProperty
        String language;

        @JsonProperty
        Map<String, Map<String, String>> i18n;

        @JsonProperty
        Map<Integer, List<SiteMapEntry>> siteMapFooter;

        @JsonProperty
        UserInfo userInfo;

        @JsonProperty
        String kernelEndPoint;

        @JsonProperty
        String accountEndPoint;

        @JsonProperty
        boolean devMode;
    }

}
