package org.oasis_eu.portal.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.oasis_eu.portal.core.PortalCorePackage;
import org.oasis_eu.portal.core.internal.PortalCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * User: schambon
 * Date: 5/30/14
 */
@Configuration
@ComponentScan(basePackageClasses = PortalCorePackage.class)
@EnableCaching
public class PortalCoreConfiguration {


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.registerModule(new JSR310Module());
        return mapper;
    }


    @Bean
    public CacheManager cacheManager() {
        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();
        PortalCacheManager portalCacheManager = new PortalCacheManager(
                Arrays.asList("appstore", "subscriptions", "user-instances", "org-instances", "user-memberships", "org-memberships"));
        portalCacheManager.afterPropertiesSet();

        compositeCacheManager.setCacheManagers(Arrays.asList(
                portalCacheManager,
                new ConcurrentMapCacheManager("organizations", "applications", "services", "services-of-instance")));
        return compositeCacheManager;
    }


}
