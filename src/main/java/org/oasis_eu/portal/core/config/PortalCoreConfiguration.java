package org.oasis_eu.portal.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.oasis_eu.portal.core.PortalCorePackage;
import org.oasis_eu.spring.util.RequestBoundCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: schambon
 * Date: 5/30/14
 */
@Configuration
@ComponentScan(basePackageClasses = PortalCorePackage.class)
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(PortalCoreConfiguration.OasisCacheConfiguration.class)
public class PortalCoreConfiguration extends CachingConfigurerSupport {

    @ConfigurationProperties(prefix = "cache")
    public static class OasisCacheConfiguration {
        private List<String> hosts;

        public List<String> getHosts() {
            return hosts;
        }

        public void setHosts(List<String> hosts) {
            this.hosts = hosts;
        }
    }

    @Autowired
    private OasisCacheConfiguration oasisCacheConfiguration;

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        return objectMapper;
    }

    @Bean
    public RequestBoundCacheManager portalCacheManager() {
        return new RequestBoundCacheManager(
            Arrays.asList(
                "appstore",
                "subscriptions",
                "user-instances",
                "org-instances",
                "user-memberships",
                "org-memberships",
                "pending-memberships",
                "services-of-instance",
                "instances",
                "accounts"));
    }


    @Bean
    public CacheManager longTermCacheManager() {
        return new HazelcastCacheManager(hazelcastInstance());
    }

    @Override
    @Bean
    public CacheManager cacheManager() {
        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();

        compositeCacheManager.setCacheManagers(Arrays.asList(
            portalCacheManager(),
            longTermCacheManager()));
        return compositeCacheManager;
    }


    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }


    @Bean
    public HazelcastInstance hazelcastInstance() {
        Map<String, MapConfig> mapConfigs = new HashMap<>();

        mapConfigs.put("organizations", getMapConfig("organizations"));
        mapConfigs.put("applications", getMapConfig("applications"));
        mapConfigs.put("sitemap", getMapConfig("sitemap"));
        mapConfigs.put("sitemapheader", getMapConfig("sitemapheader"));
        mapConfigs.put("services", getMapConfig("services"));

        Config config = new Config();
        config.setMapConfigs(mapConfigs);

        config.setNetworkConfig(networkConfig());

        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public NetworkConfig networkConfig() {

        TcpIpConfig tcpIpConfig = new TcpIpConfig().setEnabled(true).setMembers(oasisCacheConfiguration.getHosts());

        return new NetworkConfig()
            .setPort(5701)
            .setJoin(
                new JoinConfig()
                    .setMulticastConfig(new MulticastConfig().setEnabled(false))
                    .setTcpIpConfig(tcpIpConfig)
            );
    }

    private MapConfig getMapConfig(String name) {
        MapConfig config = new MapConfig();
        config.setName(name);
        config.setEvictionPolicy(EvictionPolicy.LRU);
        config.setTimeToLiveSeconds(900);
        return config;
    }

    @Bean
    @Qualifier("xmlAwareRestTemplate")
    public RestTemplate xmlAwareRestTemplate() {
        RestTemplate rt = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        rt.setMessageConverters(Arrays.asList(new XmlParserConverter()));
        return rt;
    }

}
