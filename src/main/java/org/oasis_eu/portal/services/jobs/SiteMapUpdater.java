package org.oasis_eu.portal.services.jobs;

import org.oasis_eu.portal.model.sitemap.*;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.oasis_eu.portal.config.environnements.EnvProperties;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.oasis_eu.portal.dao.SiteMapComponentsRepository;
import org.oasis_eu.portal.dao.StylePropertiesMapRepository;
import org.oasis_eu.portal.services.SiteMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 1/13/15
 */
@Service
public class SiteMapUpdater {

    private static final Logger logger = LoggerFactory.getLogger(SiteMapUpdater.class);

    @Autowired
    private SiteMapService siteMapService;

    @Autowired
    private EnvProperties envProperties;

    @Autowired
    private SiteMapComponentsRepository siteMapComponentsRepository;

    @Autowired
    private StylePropertiesMapRepository stylePropertiesMapRepository;

    @Scheduled(cron = "${web.sitemap.refresh}")
    public void reload() {
        logger.info("Reloading site map: Header, Footer");
        //parse and create new entries for website
        initializeSiteMapComponents();
        reloadFooter();
    }

    public void initializeSiteMapComponents() {
        Map<String, EnvConfig> mapEnvConfig = envProperties.getConfs();
        mapEnvConfig.forEach((website, envConfig) -> {
            SiteMapComponents siteMapComponents = siteMapComponentsRepository.findByWebsite(website);
            if (siteMapComponents == null) {
                siteMapComponents = new SiteMapComponents();
                siteMapComponents.setWebsite(website);
                siteMapComponentsRepository.save(siteMapComponents);
            }
        });
    }

    public void initializeStylePropertiesMap() {
        List<StyleProperty> styleProperties = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject("{default: " +
                        "[" +
                        "    {key: \"--primary-color\", value: \"#6f438e\"},\n" +
                        "    {key:\"--secondary-color\",value:\"#4c2d62\"},\n" +
                        "    {key: \"--tertiary-color\", value:\"#FFF\"},\n" +
                        "\n" +
                        "    {key:\"--separator-color\", value: \"#CCC\"},\n" +
                        "    {key:\"--footer-color\", value: \"#EEE\"},\n" +
                        "    {key:\"--nav-color\",value: \"#FBFBFB\"},\n" +
                        "    {key:\"--bg-content-color\",value: \"#f4f4f4\"},\n" +
                        "\n" +
                        "    /* STORE */\n" +
                        "    {key:\"--buy-color\",value: \"#ff4d37e8\"},\n" +
                        "    {key:\"--buy-dark-color\",value: \"rgba(150, 0, 42, 1)\"},\n" +
                        "\n" +
                        "    {key:\"--free-color\",value: \"#f8ba03\"},\n" +
                        "    {key:\"--free-dark-color\",value: \"#f69b04\"},\n" +
                        "\n" +
                        "    {key:\"--installed-color\",value: \"#43a047\"},\n" +
                        "    {key:\"--installed-dark-color\",value: \"#2e7d32\"},\n" +
                        "\n" +
                        "    /* BUTTON */\n" +
                        "    {key:\"--button-primary-color\",value: \"#6f438e\"}, /*Border text*/\n" +
                        "    {key:\"--button-secondary-color\",value: \"#FFF\"}, /* background */\n" +
                        "    {key:\"--button-tertiary-color\",value: \"#4c2d62\"}, /* background on hover */\n" +
                        "\n" +
                        "\n" +
                        "    /* COLOR */\n" +
                        "    {key:\"--black-color\",value: \"#4c4c4c\"},\n" +
                        "    {key:\"--alert-color\",value: \"#ff4d37e8\"},\n" +
                        "    {key:\"--gray-color\",value: \"#e4e4e4\"},\n" +
                        "\n" +
                        "\n" +
                        "    /* LOGO */\n" +
                        "    {key:\"--logo-url\",value: \"url('/img/logo-ozwillo.png')\"},\n" +
                        "    {key:\"--logo-width\",value: \"120px\"},\n" +
                        "    {key:\"--footer-logo-url\",value: 'url(\"/img/logo-ozwillo-footer.png\")'},\n" +
                        "    \n" +
                        "]" +
                    "}");

            JSONArray array = obj.getJSONArray("default");
            for (int i = 0; i< array.length(); i++){
                String key = array.getJSONObject(i).getString("key");
                String value = array.getJSONObject(i).getString("value");
                styleProperties.add(new StyleProperty(key, value));
            }
            stylePropertiesMapRepository.save(new StylePropertiesMap(styleProperties, "ozwillo"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void reloadFooter() {

        Map<String, EnvConfig> mapEnvConfig = envProperties.getConfs();
        mapEnvConfig.forEach((website, envConfig) -> {
            String url_footer = envConfig.getWeb().getSitemap().getUrl_footer();

            // Loads and updates the footer from JSON resource
            try {
                RestTemplate restTemplate = new RestTemplate();
                FooterMenuSet footerMenuSet = restTemplate.getForObject(url_footer, FooterMenuSet.class);
                Footer footer = footerMenuSet.getFooter();
                List<SiteMapMenuFooter> menuset = footer.getMenuset();
                menuset.forEach(siteMap -> {
                            List<SiteMapEntry> entries = siteMap.getEntries();
                            List<SiteMapEntry> filteredEntries =
                                    entries.stream()
                                            .filter(siteMapEntry ->
                                                    siteMapEntry != null && siteMapEntry.getUrl() != null)
                                            .collect(Collectors.toList());
                            siteMap.setEntries(filteredEntries);
                        }
                );
                menuset.forEach(menu -> siteMapService.updateSiteMapFooter(website, menu.getLanguage(), menu));
                logger.debug("Footer Loaded!");
            } catch (RestClientException rce) {
                logger.error("The Footer file was not Loaded due to error: " + rce);
            }
        });
    }
}
