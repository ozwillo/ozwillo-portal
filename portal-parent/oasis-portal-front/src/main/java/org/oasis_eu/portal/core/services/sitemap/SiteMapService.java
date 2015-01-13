package org.oasis_eu.portal.core.services.sitemap;

import org.oasis_eu.portal.core.mongo.dao.sitemap.SiteMapRepository;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMap;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 12/15/14
 */
@Service
public class SiteMapService {

    @Autowired
    private SiteMapRepository repository;

    @Value("${web.home}")
    private String webHome;

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



}
