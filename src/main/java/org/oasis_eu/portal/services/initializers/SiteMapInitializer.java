package org.oasis_eu.portal.services.initializers;

import org.oasis_eu.portal.dao.StylePropertiesMapRepository;
import org.oasis_eu.portal.services.SiteMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class SiteMapInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SiteMapInitializer.class);

    @Autowired
    private SiteMapService siteMapService;

    @Autowired
    private StylePropertiesMapRepository stylePropertiesMapRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.debug("Context refreshed, checking header and footer status !");

        siteMapService.reloadFooter();

        if(stylePropertiesMapRepository.count() == 0)
            siteMapService.initializeStylePropertiesMap();
    }
}
