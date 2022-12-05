package org.oasis_eu.portal.services;

import org.oasis_eu.portal.model.sitemap.SiteMapEntry;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/18/14
 */
@Service
public class MyNavigationService {

    private final SiteMapService siteMapService;

    private final HttpServletRequest httpRequest;

    private final EnvPropertiesService envPropertiesService;

    public MyNavigationService(SiteMapService siteMapService, HttpServletRequest httpRequest, EnvPropertiesService envPropertiesService) {
        this.siteMapService = siteMapService;
        this.httpRequest = httpRequest;
        this.envPropertiesService = envPropertiesService;
    }

    /**
     * @return a map of {@link SiteMapEntry} values keyed by the row in which they have to appear
     */
    public Map<Integer, List<SiteMapEntry>> getSiteMapFooter() {
        return getSiteMapFooter(RequestContextUtils.getLocale(httpRequest).getLanguage());
    }

    public Map<Integer, List<SiteMapEntry>> getSiteMapFooter(String language) {
        List<SiteMapEntry> siteMapEntries = siteMapService.getSiteMapFooter(envPropertiesService.getCurrentKey(),language);

        if (siteMapEntries == null) {
            return Collections.emptyMap();
        }


        return siteMapEntries.stream().collect(Collectors.groupingBy(SiteMapEntry::getRow));
    }
}
