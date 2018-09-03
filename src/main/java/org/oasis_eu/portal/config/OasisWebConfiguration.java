package org.oasis_eu.portal.config;

import de.javakaffee.web.msm.MemcachedBackupSessionManager;
import org.oasis_eu.spring.kernel.rest.ResponseProviderInterceptor;
import org.oasis_eu.spring.kernel.security.TokenRefreshInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Configuration
public class OasisWebConfiguration implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(OasisWebConfiguration.class);

    @Autowired
    ApplicationContext applicationContext;

    @Value("${ha.enabled:false}")
    private boolean highAvailability; // are we in HA mode?
    @Value("${ha.nodes:}")
    private String nodes;            // memcached nodes eg n1:oasis-portal-1:11211,n2:oasis-portal-2:11211
    @Value("${ha.failover:}")
    private String failover;         // failover node, should be the node corresponding to localhost

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OasisLocaleInterceptor());
        registry.addInterceptor(tokenRefreshInterceptor());
        registry.addInterceptor(new ResponseProviderInterceptor()); // provides HTTP response as request attribute
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/my").setViewName("forward:/index.html");
        registry.addViewController("/my/organization/**").setViewName("forward:/index.html");
        registry.addViewController("/my/profile").setViewName("forward:/index.html");
        registry.addViewController("/my/notif").setViewName("forward:/index.html");
        registry.addViewController("/my/dashboard").setViewName("forward:/index.html");
        registry.addViewController("/my/dashboard/**").setViewName("forward:/index.html");
        registry.addViewController("/**/store").setViewName("forward:/index.html");
        registry.addViewController("/**/store/**").setViewName("forward:/index.html");
    }

    @Bean
    public TokenRefreshInterceptor tokenRefreshInterceptor() {
        return new TokenRefreshInterceptor();
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new OasisLocaleResolver();
    }

    @Bean
    public WebServerFactoryCustomizer containerCustomizer() {
        return factory -> {
            logger.info("Customizing Tomcat container");

            if (highAvailability) {
                logger.info("Setting up high availability configuration");
                TomcatServletWebServerFactory containerFactory = (TomcatServletWebServerFactory) factory;
                TomcatContextCustomizer tomcatContextCustomizer = context -> {
                    context.setSessionTimeout(30);
                    context.setManager(new MemcachedBackupSessionManager() {{
                        setMemcachedNodes(nodes);
                        setFailoverNodes(failover);
                        setRequestUriIgnorePattern(".*\\.(ico|png|gif|jpg|css|js)$");
                    }});
                };
                containerFactory.setTomcatContextCustomizers(Collections.singletonList(tomcatContextCustomizer));
            } else {
                logger.info("Skipping HA configuration");
            }
        };
    }
}
