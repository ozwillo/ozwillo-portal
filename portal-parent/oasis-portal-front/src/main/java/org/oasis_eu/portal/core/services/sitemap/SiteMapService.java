package org.oasis_eu.portal.core.services.sitemap;

import org.oasis_eu.portal.core.mongo.dao.sitemap.SiteMapRepository;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMap;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User: schambon
 * Date: 12/15/14
 */
@Service
public class SiteMapService {

    @Autowired
    private SiteMapRepository repository;

    @Cacheable(value = "sitemap", key = "#language")
    public List<SiteMapEntry> getSiteMap(String language) {
        SiteMap siteMap = repository.findByLanguage(language);
        if (siteMap != null) {
            return siteMap.getEntries();
        } else {
            return null;
        }
    }


    @CacheEvict(value = "sitemap", key = "#language")
    public void updateSiteMap(String language) {
        // to implement in a second step
    }
}
