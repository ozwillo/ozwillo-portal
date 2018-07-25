package org.oasis_eu.portal.config;

import de.javakaffee.web.msm.MemcachedBackupSessionManager;
import org.oasis_eu.spring.kernel.rest.ResponseProviderInterceptor;
import org.oasis_eu.spring.kernel.security.TokenRefreshInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.MimeMappings;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Collections;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Configuration
public class OasisWebConfiguration extends WebMvcConfigurerAdapter {

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
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        if (highAvailability) {
            logger.info("Setting up high availability configuration");
            return factory -> {
                logger.info("Customizing Tomcat container");

                TomcatEmbeddedServletContainerFactory containerFactory = (TomcatEmbeddedServletContainerFactory) factory;
                TomcatContextCustomizer tomcatContextCustomizer = context -> {
                    context.setSessionTimeout(30);
                    context.setManager(new MemcachedBackupSessionManager() {{
                        setMemcachedNodes(nodes);
                        setFailoverNodes(failover);
                        setRequestUriIgnorePattern(".*\\.(ico|png|gif|jpg|css|js)$");
                    }});
                };
                containerFactory.setTomcatContextCustomizers(Collections.singletonList(tomcatContextCustomizer));

                setMimeMappings(factory);
            };
        } else {
            logger.info("Skipping HA configuration");
            return this::setMimeMappings;
        }
    }

    // FIXME : probably not needed anymore since webfonts are loaded from Google
    private void setMimeMappings(ConfigurableEmbeddedServletContainer factory) {
        MimeMappings mm = new MimeMappings(MimeMappings.DEFAULT);
        mm.add("woff", "application/font-woff");
        mm.add("woff2", "application/font-woff2;");

        factory.setMimeMappings(mm);
    }

}
