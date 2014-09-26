package org.oasis_eu.portal.config;

import org.oasis_eu.spring.config.OasisSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * User: schambon
 * Date: 5/13/14
 */
@Configuration
public class OasisPortalSecurity extends OasisSecurityConfiguration {

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
