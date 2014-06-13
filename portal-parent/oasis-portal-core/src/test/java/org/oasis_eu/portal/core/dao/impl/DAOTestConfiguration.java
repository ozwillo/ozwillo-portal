package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.spring.config.KernelConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Configuration
@Import(KernelConfiguration.class)
@ComponentScan(basePackages = "org.oasis_eu.portal.core")
@PropertySource("classpath:test-application.properties")
public class DAOTestConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
