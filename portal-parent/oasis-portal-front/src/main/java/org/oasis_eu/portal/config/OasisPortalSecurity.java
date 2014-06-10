package org.oasis_eu.portal.config;

import org.oasis_eu.spring.config.OASISSecurityConfiguration;
import org.oasis_eu.spring.kernel.security.OASISExceptionTranslationConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * User: schambon
 * Date: 5/13/14
 */
@Configuration
public class OasisPortalSecurity extends OASISSecurityConfiguration {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .logout().logoutSuccessHandler(logoutHandler()).and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint()).and()
                .authorizeRequests()
                .antMatchers("/dash/**").authenticated()
                .anyRequest().permitAll().and()
                .addFilterBefore(openIdCAuthFilter(), AbstractPreAuthenticatedProcessingFilter.class);

        OASISExceptionTranslationConfigurer configurer = oasisExceptionTranslationConfigurer();

        http.apply(configurer);

    }
}
