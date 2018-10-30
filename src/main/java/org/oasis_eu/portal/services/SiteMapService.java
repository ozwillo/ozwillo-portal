package org.oasis_eu.portal.services;

import org.oasis_eu.portal.dao.SiteMapComponentsRepository;
import org.oasis_eu.portal.model.sitemap.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private SiteMapComponentsRepository siteMapComponentsRepository;

    @Autowired
    private EnvPropertiesService envPropertiesService;


    @Cacheable(value = "sitemapheader", key = "#website + #language.toString()")
    public SiteMapMenuHeader getSiteMapHeader(String website, String language) {
        SiteMapMenuHeader siteMapMenuHeader = siteMapComponentsRepository.findByWebsite(website)
                .getSiteMapMenuHeader()
                .stream()
                .filter(header -> header.getLanguage().equals(language))
                .findFirst()
                .orElse(null);


        if (siteMapMenuHeader != null) {
            siteMapMenuHeader.getItems().forEach(this::setHyperLinks);
        }

        return siteMapMenuHeader;
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
            String webHome = envPropertiesService.getCurrentConfig().getWeb().getHome();
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


    @CacheEvict(value = "sitemapheader", key ="#website.toString() + #language.toString()")
    public void updateSiteMapHeader(String website, String language, SiteMapMenuHeader siteMapheader) {

        SiteMapComponents siteMapComponents = siteMapComponentsRepository.findByWebsite(website);

        List<SiteMapMenuHeader> siteMapMenuHeaders = siteMapComponents.getSiteMapMenuHeader();

        SiteMapMenuHeader siteMapMenuHeader =
                siteMapMenuHeaders
                        .stream()
                        .filter(header -> header.getLanguage().equals(language))
                        .findFirst()
                        .orElse(null);

        if (siteMapMenuHeader == null)
            siteMapComponents.getSiteMapMenuHeader().add(siteMapheader);

        siteMapMenuHeader = siteMapheader;
        siteMapMenuHeader.setId(null);
        siteMapMenuHeader.setLanguage(language);

        siteMapComponentsRepository.save(siteMapComponents);
    }

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
