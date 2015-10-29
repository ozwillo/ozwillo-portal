package org.oasis_eu.portal.core.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oasis_eu.portal.core.PortalCorePackage;
import org.oasis_eu.spring.util.RequestBoundCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

/**
 * User: schambon
 * Date: 5/30/14
 */
@Configuration
@ComponentScan(basePackageClasses = PortalCorePackage.class)
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(PortalCoreConfiguration.OasisCacheConfiguration.class)
public class PortalCoreConfiguration implements CachingConfigurer {

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
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JodaModule());
		mapper.registerModule(new JSR310Module());

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
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

	public MapConfig getMapConfig(String name) {
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
