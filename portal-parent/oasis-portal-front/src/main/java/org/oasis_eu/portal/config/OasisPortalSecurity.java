package org.oasis_eu.portal.config;

import org.oasis_eu.spring.config.OasisSecurityConfiguration;
import org.oasis_eu.spring.kernel.security.OasisAuthenticationFilter;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import java.util.Arrays;

/**
 * User: schambon
 * Date: 5/13/14
 */
@Configuration
public class OasisPortalSecurity extends OasisSecurityConfiguration {

    @Bean
    @Primary
    public OpenIdCConfiguration openIdCConfiguration() {
        StaticOpenIdCConfiguration configuration = new PortalOpenIdCConfiguration();
        configuration.addSkippedPaths(Arrays.asList("/img/", "/js/", "/css/", "/status", "/api/"));
        return configuration;
    }


    @Override
    public OasisAuthenticationFilter oasisAuthenticationFilter() throws Exception {
        OasisAuthenticationFilter filter = super.oasisAuthenticationFilter();
        filter.setSuccessHandler(new OasisPortalAuthenticationSuccessHandler());
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .logout().logoutSuccessHandler(logoutHandler()).and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint()).and()
                .authorizeRequests()
                .antMatchers("/my/**").authenticated()
                .anyRequest().permitAll().and()
                .addFilterBefore(oasisAuthenticationFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .addFilterAfter(oasisExceptionTranslationFilter(authenticationEntryPoint()), ExceptionTranslationFilter.class);

    }
}
