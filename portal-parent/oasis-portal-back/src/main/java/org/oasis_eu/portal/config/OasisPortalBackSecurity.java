package org.oasis_eu.portal.config;

import org.oasis_eu.spring.config.OasisSecurityConfiguration;
import org.oasis_eu.spring.kernel.security.OASISExceptionTranslationConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * 
 * @author mkalamalami
 *
 */
@Configuration
public class OasisPortalBackSecurity extends OasisSecurityConfiguration {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        OASISExceptionTranslationConfigurer configurer = oasisExceptionTranslationConfigurer();
        http.apply(configurer);
    }
}
