package org.oasis_eu.portal.services.jobs;

import org.oasis_eu.portal.services.SiteMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * User: schambon
 * Date: 1/13/15
 */
@Service
@Profile("!test")
public class SiteMapUpdater {

    private static final Logger logger = LoggerFactory.getLogger(SiteMapUpdater.class);

    @Autowired
    private SiteMapService siteMapService;

    @Scheduled(cron = "${web.sitemap.refresh}")
    public void reload() {
        logger.info("Reloading site map footer");
        //parse and create new entries for website
        siteMapService.initializeSiteMapComponents();
        siteMapService.reloadFooter();
    }

}
