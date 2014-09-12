package org.oasis_eu.portal.core.internal;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * User: schambon
 * Date: 9/12/14
 */
public class RequestBoundCache implements Cache {

    private static final String KEY = "oasis-portal-requestcache-";

    private String name;

    public RequestBoundCache(String name) {
        this.name = name;
    }

    private Cache target() {


        Cache cache = (Cache) RequestContextHolder.getRequestAttributes().getAttribute(KEY + name, RequestAttributes.SCOPE_REQUEST);
        if (cache == null) {
            cache = new ConcurrentMapCache(name, true);
            RequestContextHolder.getRequestAttributes().setAttribute(KEY + name, cache, RequestAttributes.SCOPE_REQUEST);
        }
        return cache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return target().getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        return target().get(key);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return target().get(key, type);
    }

    @Override
    public void put(Object key, Object value) {
        target().put(key, value);
    }

    @Override
    public void evict(Object key) {
        target().evict(key);
    }

    @Override
    public void clear() {
        target().clear();
    }
}
