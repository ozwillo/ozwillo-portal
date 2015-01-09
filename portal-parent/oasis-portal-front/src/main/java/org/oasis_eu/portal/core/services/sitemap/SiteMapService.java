package org.oasis_eu.portal.core.services.sitemap;

import org.oasis_eu.portal.core.mongo.dao.sitemap.SiteMapRepository;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMap;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapEntry;
import org.oasis_eu.portal.core.services.sitemap.xml.Footer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 12/15/14
 */
@Service
public class SiteMapService {

    private static final Logger logger = LoggerFactory.getLogger(SiteMapService.class);

    @Autowired
    private SiteMapRepository repository;

    @Value("${web.sitemap.url}")
    private String sitemapUrl;

    @Value("${web.home}")
    private String webHome;

    @Autowired
    @Qualifier("xmlAwareRestTemplate")
    private RestTemplate restTemplate;

    @Cacheable(value = "sitemap", key = "#language")
    public List<SiteMapEntry> getSiteMap(String language) {
        SiteMap siteMap = repository.findByLanguage(language);
        if (siteMap != null) {
            return siteMap.getEntries()
                    .stream()
                    .map(this::fixSME)
                    .collect(Collectors.toList());

        } else {
            return null;
        }
    }

    private SiteMapEntry fixSME(SiteMapEntry entry) {
        if (entry == null) return new SiteMapEntry();

        entry.setUrl(webHome + entry.getUrl());
        return entry;
    }


    @CacheEvict(value = "sitemap", key = "#language")
    public void updateSiteMap(String language, SiteMap siteMap) {
        SiteMap old = repository.findByLanguage(language);
        if (old != null) {
            repository.delete(old.getId());
        }

        siteMap.setLanguage(language);
        siteMap.setId(null);

        repository.save(siteMap);
    }

    @Scheduled(cron = "${web.sitemap.refresh}")
    public void reload() {
        logger.info("Reloading site map");

        restTemplate.getForObject(sitemapUrl, Footer.class).getMenuset().forEach(menu -> updateSiteMap(menu.getLanguage(), menu));

    }

}
