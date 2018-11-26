package org.oasis_eu.portal.services;

import org.oasis_eu.portal.model.sitemap.SiteMapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
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

    @Autowired
    private EnvPropertiesService envPropertiesService;

    /**
     * @return a map of {@link SiteMapEntry} values keyed by the row in which they have to appear
     */
    public Map<Integer, List<SiteMapEntry>> getSiteMapFooter() {
        return getSiteMapFooter(RequestContextUtils.getLocale(httpRequest).getLanguage());
    }

    public Map<Integer, List<SiteMapEntry>> getSiteMapFooter(String language) {
        List<SiteMapEntry> siteMapEntries = siteMapService.getSiteMapFooter(envPropertiesService.extractEnvKey(),language);

        if (siteMapEntries == null) {
            return Collections.emptyMap();
        }


        return siteMapEntries.stream().collect(Collectors.groupingBy(SiteMapEntry::getRow));
    }
}
