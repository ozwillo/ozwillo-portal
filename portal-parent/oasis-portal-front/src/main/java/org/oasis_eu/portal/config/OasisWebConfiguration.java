package org.oasis_eu.portal.config;

import java.util.EnumSet;

import org.oasis_eu.spring.kernel.security.TokenRefreshInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Configuration
public class OasisWebConfiguration extends WebMvcConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OasisWebConfiguration.class);

    @Autowired
    ApplicationContext applicationContext;

    @Value("${application.ha}") private boolean highAvailability; // are we in HA mode?

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OasisLocaleInterceptor());
        registry.addInterceptor(tokenRefreshInterceptor());
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
    public ServletContextInitializer servletContextInitializer() {
        return (ServletContext servletContext) -> {
            final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
            characterEncodingFilter.setEncoding("UTF-8");
            characterEncodingFilter.setForceEncoding(true);
            servletContext.addFilter("characterEncodingFilter", characterEncodingFilter).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
        };
    }


    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        if (highAvailability) {
            logger.info("Setting up high availability configuration");
            return factory -> {
                logger.info("Customizing Tomcat container");
            };
        } else {
            logger.info("Skipping HA configuration");
            return factory -> {};
        }
    }

}
