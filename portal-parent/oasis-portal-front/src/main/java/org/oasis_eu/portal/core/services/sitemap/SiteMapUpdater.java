package org.oasis_eu.portal.core.services.sitemap;

import org.oasis_eu.portal.core.services.sitemap.xml.Footer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * User: schambon
 * Date: 1/13/15
 */
@Service
public class SiteMapUpdater {

    private static final Logger logger = LoggerFactory.getLogger(SiteMapUpdater.class);

    @Autowired
    private SiteMapService siteMapService;

    @Value("${web.sitemap.url}")
    private String sitemapUrl;

    @Autowired
    @Qualifier("xmlAwareRestTemplate")
    private RestTemplate restTemplate;


    @Scheduled(cron = "${web.sitemap.refresh}")
    public void reload() {
        logger.info("Reloading site map");

        restTemplate.getForObject(sitemapUrl, Footer.class).getMenuset().forEach(menu -> siteMapService.updateSiteMap(menu.getLanguage(), menu));

    }

}
