package org.oasis_eu.portal.core.internal;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 9/12/14
 */
public class PortalCacheManager extends AbstractCacheManager {

    private List<String> requestBoundCaches;

    @Override
    public Collection<String> getCacheNames() {
        return requestBoundCaches;
    }

    public PortalCacheManager(List<String> requestBoundCaches) {
        this.requestBoundCaches = requestBoundCaches;
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        return requestBoundCaches.stream().map(cacheName -> new RequestBoundCache(cacheName)).collect(Collectors.toList());
    }
}
