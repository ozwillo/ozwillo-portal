package org.oasis_eu.portal.config;

import org.oasis_eu.spring.config.OasisSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.ExceptionTranslationFilter;

/**
 * 
 * @author mkalamalami
 *
 */
@Configuration
public class OasisPortalBackSecurity extends OasisSecurityConfiguration {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(oasisExceptionTranslationFilter(authenticationEntryPoint()), ExceptionTranslationFilter.class);
    }
}
