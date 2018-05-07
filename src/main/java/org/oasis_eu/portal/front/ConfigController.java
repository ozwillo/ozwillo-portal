package org.oasis_eu.portal.front;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapEntry;
import org.oasis_eu.portal.front.generic.BaseController;
import org.oasis_eu.portal.front.generic.i18nMessages;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
public class ConfigController extends BaseController {

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

    @GetMapping("/config")
    public MyConfig getConfig(HttpServletRequest request) throws JsonProcessingException {
        // trad
        Locale locale = RequestContextUtils.getLocale(request);
        Map<String, Map<String, String>> i18n = new HashMap<>();
        i18n.put(locale.getLanguage(), i18nMessages.getI18n_all(locale, messageSource));

        //Site map footer
        Map<Integer, List<SiteMapEntry>> siteMapFooter = navigationService.getSiteMapFooter();

        //MyConfig object
        MyConfig myConfig = new MyConfig();
        myConfig.language = locale.getLanguage();
        myConfig.i18n = i18n;
        myConfig.siteMapFooter = siteMapFooter;
        myConfig.kernelEndPoint = kernelEndPoint;
        myConfig.accountEndPoint = accountEndPoint;
        myConfig.devMode = devMode;

        return myConfig;
    }

    public static class MyConfig {
        @JsonProperty
        String language;

        @JsonProperty
        Map<String, Map<String, String>> i18n;

        @JsonProperty
        Map<Integer, List<SiteMapEntry>> siteMapFooter;

        @JsonProperty
        String kernelEndPoint;

        @JsonProperty
        String accountEndPoint;

        @JsonProperty
        boolean devMode;
    }

}
