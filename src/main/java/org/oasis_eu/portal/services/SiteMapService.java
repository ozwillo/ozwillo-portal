package org.oasis_eu.portal.services;

import org.oasis_eu.portal.dao.SiteMapComponentsRepository;
import org.oasis_eu.portal.model.sitemap.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger logger = LoggerFactory.getLogger(SiteMapService.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SiteMapComponentsRepository siteMapComponentsRepository;

    @Autowired
    private EnvPropertiesService envPropertiesService;

    @Cacheable(value = "sitemapfooter", key = "#website.toString() + #language.toString()")
    public List<SiteMapEntry> getSiteMapFooter(String website, String language) {
        SiteMapMenuFooter siteMapMenuFooter = siteMapComponentsRepository.findByWebsite(website)
                .getSiteMapMenuFooter()
                .stream()
                .filter(footer -> footer.getLanguage().equals(language))
                .findFirst()
                .orElse(null);


        if (siteMapMenuFooter != null) {
            return siteMapMenuFooter.getEntries()
                    .stream()
                    .map(this::fixSME)
                    .collect(Collectors.toList());
        }else{
            return null;
        }
    }

    private SiteMapEntry fixSME(SiteMapEntry entry) {
        if (entry == null) return new SiteMapEntry();

        String webHome = envPropertiesService.getCurrentConfig().getWeb().getHome();
        entry.setUrl(webHome + entry.getUrl());
        return entry;
    }

    @CacheEvict(value = "sitemapfooter", key = "#website.toString() + #language.toString()")
    public void updateSiteMapFooter(String website, String language, SiteMapMenuFooter siteMapFooter) {

        SiteMapComponents siteMapComponents = siteMapComponentsRepository.findByWebsite(website);

        List<SiteMapMenuFooter> siteMapMenuFooters = siteMapComponents.getSiteMapMenuFooter();

        SiteMapMenuFooter siteMapMenuFooter =
                siteMapMenuFooters
                        .stream()
                        .filter(header -> header.getLanguage().equals(language))
                        .findFirst()
                        .orElse(null);

        if (siteMapMenuFooter == null)
            siteMapComponents.getSiteMapMenuFooter().add(siteMapFooter);

        siteMapMenuFooter = siteMapFooter;
        siteMapMenuFooter.setId(null);
        siteMapMenuFooter.setLanguage(language);

        siteMapComponentsRepository.save(siteMapComponents);
    }
}
