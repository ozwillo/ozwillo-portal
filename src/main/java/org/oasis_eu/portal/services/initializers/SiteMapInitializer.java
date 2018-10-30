package org.oasis_eu.portal.services.initializers;

import org.oasis_eu.portal.dao.GoogleAnalyticsTagRepository;
import org.oasis_eu.portal.dao.SiteMapHeaderRepository;
import org.oasis_eu.portal.dao.SiteMapRepository;
import org.oasis_eu.portal.dao.StylePropertiesMapRepository;
import org.oasis_eu.portal.services.jobs.SiteMapUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class SiteMapInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SiteMapInitializer.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SiteMapRepository footerRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SiteMapHeaderRepository headerRepository;

    @Autowired
    private SiteMapUpdater siteMapUpdater;

    @Autowired
    private StylePropertiesMapRepository stylePropertiesMapRepository;

    @Autowired
    private GoogleAnalyticsTagRepository googleAnalyticsTagRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.debug("Context refreshed, checking header and footer status !");

        siteMapUpdater.initializeSiteMapComponents();

        if (headerRepository.count() == 0)
            siteMapUpdater.reloadHeader();

        if (footerRepository.count() == 0)
            siteMapUpdater.reloadFooter();

        if(stylePropertiesMapRepository.count() == 0)
            siteMapUpdater.initializeStylePropertiesMap();

        if(googleAnalyticsTagRepository.count() == 0)
            siteMapUpdater.initializeGoogleAnalyticsTags();
    }
}
