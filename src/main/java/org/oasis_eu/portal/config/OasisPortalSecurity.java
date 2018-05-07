package org.oasis_eu.portal.config;

import org.oasis_eu.spring.config.OasisSecurityConfiguration;
import org.oasis_eu.spring.kernel.security.OasisAuthenticationFilter;
import org.oasis_eu.spring.kernel.security.OpenIdCConfiguration;
import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Arrays;

/**
 * User: schambon
 * Date: 5/13/14
 */
@Configuration
public class OasisPortalSecurity extends OasisSecurityConfiguration {

    @Value("${application.security.noauthdevmode:false}")
    private boolean noauthdevmode;
    @Value("${application.devmode:false}")
    private boolean devmode;

    @Bean
    @Primary
    public OpenIdCConfiguration openIdCConfiguration() {
        StaticOpenIdCConfiguration configuration = new PortalOpenIdCConfiguration();
        configuration.addSkippedPaths(Arrays.asList("/img/", "/js/", "/css/", "/status", "/api/", "/build/"));
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
        if (noauthdevmode && devmode) {
            // don't configure any security
        } else {
            http
                .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessHandler(logoutHandler()).and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint()).and()
                .authorizeRequests()
                .antMatchers("/api/organization/import").permitAll()
                .antMatchers("/my/**").authenticated()
                .anyRequest().permitAll().and()
                .csrf().ignoringAntMatchers("/api/organization/import").and()
                .addFilterBefore(oasisAuthenticationFilter(), AbstractPreAuthenticatedProcessingFilter.class);
        }
        http
            .addFilterAfter(oasisExceptionTranslationFilter(authenticationEntryPoint()), ExceptionTranslationFilter.class);
    }
}
