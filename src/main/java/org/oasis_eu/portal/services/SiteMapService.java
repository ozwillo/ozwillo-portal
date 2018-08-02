package org.oasis_eu.portal.services;

import org.oasis_eu.portal.dao.SiteMapHeaderRepository;
import org.oasis_eu.portal.dao.SiteMapRepository;
import org.oasis_eu.portal.model.sitemap.SiteMap;
import org.oasis_eu.portal.model.sitemap.SiteMapEntry;
import org.oasis_eu.portal.model.sitemap.SiteMapMenuItem;
import org.oasis_eu.portal.model.sitemap.SiteMapMenuSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 12/15/14
 */
@Service
public class SiteMapService {
    private static final Logger logger = LoggerFactory.getLogger(SiteMapService.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SiteMapRepository footerRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SiteMapHeaderRepository headerRepository;

    @Value("${web.home}")
    private String webHome;

    @Cacheable(value = "sitemapheader", key = "#language")
    public SiteMapMenuSet getSiteMapHeader(String language) {
        SiteMapMenuSet menuset = headerRepository.findByLanguage(language);
        if (menuset != null) {
            menuset.getItems().forEach(this::setHyperLinks);
        }

        return menuset;
    }

    /**
     * Complete the hyperlinks with the current root url (https://www.ozwillo-dev.eu, https://www.ozwillo-preprod.eu, etc)
     *
     * @return SiteMapMenuItem
     */
    private SiteMapMenuItem setHyperLinks(SiteMapMenuItem entry) {
        if (entry == null) return new SiteMapMenuItem();

        // complete relative URLs
        if (entry.getUrl().startsWith("/") || entry.getUrl().isEmpty()) {
            entry.setUrl(webHome + entry.getUrl());
        }

        // get root url to compute the complete img url
        try {
            if (!entry.getUrl().isEmpty() && entry.getImgUrl().startsWith("/")) {
                URL aURL = new URL(entry.getUrl());
                String url = aURL.getProtocol() + "://" + aURL.getAuthority();
                entry.setImgUrl(url + entry.getImgUrl());
            }
        } catch (MalformedURLException e) {
            logger.error("An error as occurred with URL {" + entry.getUrl() + "}. Error: " + e.getMessage());
        }

        entry.getItems().forEach(this::setHyperLinks);

        return entry;
    }


    @CacheEvict(value = "sitemapheader", key = "#language")
    public void updateSiteMapHeader(String language, SiteMapMenuSet siteMapheadaer) {
        SiteMapMenuSet old = headerRepository.findByLanguage(language);
        if (old != null) {
            headerRepository.deleteById(old.getId());
        }

        siteMapheadaer.setLanguage(language);
        siteMapheadaer.setId(null);

        headerRepository.save(siteMapheadaer);
    }

    @Cacheable(value = "sitemap", key = "#language")
    public List<SiteMapEntry> getSiteMapFooter(String language) {
        SiteMap siteMap = footerRepository.findByLanguage(language);
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
    public void updateSiteMapFooter(String language, SiteMap siteMap) {
        SiteMap old = footerRepository.findByLanguage(language);
        if (old != null) {
            footerRepository.deleteById(old.getId());
        }

        siteMap.setLanguage(language);
        siteMap.setId(null);

        footerRepository.save(siteMap);
    }
}
