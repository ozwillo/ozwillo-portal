package org.oasis_eu.portal.core.services.sitemap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.oasis_eu.portal.core.mongo.dao.sitemap.SiteMapHeaderRepository;
import org.oasis_eu.portal.core.mongo.dao.sitemap.SiteMapRepository;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMap;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapEntry;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapMenuItem;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapMenuSet;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapSubMenuEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * User: schambon
 * Date: 12/15/14
 */
@Service
public class SiteMapService {
	private static final Logger logger = LoggerFactory.getLogger(SiteMapService.class);

	@Autowired
	private SiteMapRepository repository;

	@Autowired
	private SiteMapHeaderRepository headerRepository;

	@Value("${web.home}")
	private String webHome;

	// Header

	@Cacheable(value = "sitemapheader", key = "#language")
	public SiteMapMenuSet getSiteMapHeader(String language) {
		SiteMapMenuSet menuset = headerRepository.findByLanguage(language);
		if (menuset != null) {
			menuset.getItems().forEach(si -> setHyperLinks(si) );
			menuset.getSubmenus().forEach(sm -> setHLSubMenus(sm));
		}
		return menuset;
	}

	private SiteMapSubMenuEntry setHLSubMenus(SiteMapSubMenuEntry submenuEntry) {
		submenuEntry.setUrl(webHome + submenuEntry.getUrl());
		submenuEntry.getSubItems().forEach(si -> setHyperLinks(si) );

		return submenuEntry;
	}

	/**
	 * Complete the hyperlinks with the current root url (https://www.ozwillo-dev.eu, https://www.ozwillo-preprod.eu, etc)
	 * @param entry
	 * @return SiteMapMenuItem
	 */
	private SiteMapMenuItem setHyperLinks(SiteMapMenuItem entry) {
		if (entry == null) return new SiteMapMenuItem();
		if (entry.getUrl().startsWith("/") || entry.getUrl().isEmpty()){
			entry.setUrl(webHome + entry.getUrl());
		}

		// get root url to compute the complete img url
		try {
			if ( !entry.getUrl().isEmpty() && entry.getImg_url().startsWith("/") ){
				URL aURL = new URL(entry.getUrl());
				String url = aURL.getProtocol() + "://" + aURL.getAuthority() ;
				entry.setImg_url(url + entry.getImg_url());
			}
		} catch (MalformedURLException e) {
			logger.error("An error as occurred with URL {"+entry.getUrl()+"}. Error: " + e.getMessage());
		}

		return entry;
	}


	@CacheEvict(value = "sitemapheader", key = "#language")
	public void updateSiteMapHeader(String language, SiteMapMenuSet siteMapheadaer) {
		SiteMapMenuSet old = headerRepository.findByLanguage(language);
		if (old != null) {
			headerRepository.delete(old.getId());
		}

		siteMapheadaer.setLanguage(language);
		siteMapheadaer.setId(null);

		headerRepository.save(siteMapheadaer);
	}


	// Footer

	@Cacheable(value = "sitemap", key = "#language")
	public List<SiteMapEntry> getSiteMapFooter(String language) {
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
	public void updateSiteMapFooter(String language, SiteMap siteMap) {
		SiteMap old = repository.findByLanguage(language);
		if (old != null) {
			repository.delete(old.getId());
		}

		siteMap.setLanguage(language);
		siteMap.setId(null);

		repository.save(siteMap);
	}


}
