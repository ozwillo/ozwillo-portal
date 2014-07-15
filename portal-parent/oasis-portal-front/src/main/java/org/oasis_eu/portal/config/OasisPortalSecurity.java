package org.oasis_eu.portal.config;

import org.oasis_eu.spring.config.OasisSecurityConfiguration;
import org.oasis_eu.spring.kernel.security.OASISExceptionTranslationConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
                .antMatchers("/appstore/**").authenticated()
                .anyRequest().permitAll().and()
                .addFilterBefore(openIdCAuthFilter(), AbstractPreAuthenticatedProcessingFilter.class);

        OASISExceptionTranslationConfigurer configurer = oasisExceptionTranslationConfigurer();

        http.apply(configurer);

        // TEMP HACK TO WORK OFFLINE
//        http.authorizeRequests().anyRequest().permitAll();

    }
}
