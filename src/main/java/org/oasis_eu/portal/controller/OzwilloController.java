package org.oasis_eu.portal.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.model.Languages;
import org.oasis_eu.portal.model.sitemap.SiteMapEntry;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.oasis_eu.portal.dao.StylePropertiesMapRepository;
import org.oasis_eu.portal.model.sitemap.*;
import org.oasis_eu.portal.services.EnvPropertiesService;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class OzwilloController {

    private final MyNavigationService navigationService;

    private final UserInfoService userInfoService;

    @Value("${kernel.base_uri}")
    private String kernelEndPoint;

    @Value("${kernel.account_uri}")
    private String accountEndPoint;

    // TODO : check it still has uses
    @Value("${application.devmode:false}")
    private boolean devMode;

    @Value("${application.notificationsEnabled:true}")
    private boolean notificationsEnabled;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private EnvPropertiesService envPropertiesService;

    @Autowired
    private StylePropertiesMapRepository stylePropertiesMapRepository;

    @Autowired
    public OzwilloController(MyNavigationService navigationService, UserInfoService userInfoService) {
        this.navigationService = navigationService;
        this.userInfoService = userInfoService;
    }

    @GetMapping("/user")
    public UserInfo getCurrentUser() {
        return userInfoService.currentUser();
    }

    @GetMapping("/config")
    public Config getConfig() {
        EnvConfig envConfig = envPropertiesService.getCurrentConfig();

        Locale locale = RequestContextUtils.getLocale(request);
        List<String> languages = Arrays.stream(Languages.values())
                .map(l -> l.getLocale().getLanguage()).collect(Collectors.toList());

        Map<Integer, List<SiteMapEntry>> siteMapFooter = navigationService.getSiteMapFooter();

        Config config = new Config();
        config.language = locale.getLanguage();
        config.languages = languages;
        config.siteMapFooter = siteMapFooter;
        config.kernelEndPoint = kernelEndPoint;
        config.accountEndPoint = accountEndPoint;
        config.opendataEndPoint = envConfig.getOpendata().getUrl();
        config.devMode = devMode;
        config.notificationsEnabled = notificationsEnabled;

        return config;
    }

    @GetMapping("/config/style")
    public List<StyleProperty> getStyleProperties() {
        String website = envPropertiesService.getCurrentKey();
        StylePropertiesMap stylePropertiesMap = stylePropertiesMapRepository.findByWebsite(website);
        if(stylePropertiesMap != null && !stylePropertiesMap.getStyleProperties().isEmpty()){
            return stylePropertiesMap.getStyleProperties();
        }else{
            return stylePropertiesMapRepository.findByWebsite("ozwillo").getStyleProperties();
        }
    }

    @GetMapping("/config/googleTag")
    public String getGoogleAnalyticsTag() {
        return envPropertiesService.getCurrentConfig().getWeb().getGoogleTag();
    }


    @GetMapping("/config/language/{lang}")
    public Config getLanguage(@PathVariable String lang) {
        //Site map
        Map<Integer, List<SiteMapEntry>> siteMapFooter = navigationService.getSiteMapFooter(lang);

        Config config = new Config();
        config.siteMapFooter = siteMapFooter;
        return config;
    }

    public static class Config {
        @JsonProperty
        String language;

        @JsonProperty
        List<String> languages;

        @JsonProperty
        Map<Integer, List<SiteMapEntry>> siteMapFooter;

        @JsonProperty
        String kernelEndPoint;

        @JsonProperty
        String accountEndPoint;

        @JsonProperty
        String opendataEndPoint;

        @JsonProperty
        List<StyleProperty> styleProperties;

        @JsonProperty
        boolean devMode;

        @JsonProperty
        boolean notificationsEnabled;
    }
}
