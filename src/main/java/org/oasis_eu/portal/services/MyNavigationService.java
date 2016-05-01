package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapEntry;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapMenuSet;
import org.oasis_eu.portal.core.services.sitemap.SiteMapService;
import org.oasis_eu.portal.model.MyNavigation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/18/14
 */
@Service
public class MyNavigationService {

	@Autowired
	private SiteMapService siteMapService;

	@Autowired
	private HttpServletRequest httpRequest;

	private List<String> pages = Arrays.asList("dashboard", "profile", "network", "apps");

	public List<MyNavigation> getNavigation(String pagename) {
		return pages.stream().map(id -> new MyNavigation(id, id.equals(pagename))).collect(Collectors.toList());
	}

	public List<SiteMapEntry> getSiteMapFooter() {
		return siteMapService.getSiteMapFooter(RequestContextUtils.getLocale(httpRequest).getLanguage());
	}

	public SiteMapMenuSet getSiteMapHeader() {
		return siteMapService.getSiteMapHeader(RequestContextUtils.getLocale(httpRequest).getLanguage());
	}
}
