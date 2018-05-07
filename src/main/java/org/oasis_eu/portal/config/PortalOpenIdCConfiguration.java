package org.oasis_eu.portal.config;

import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;
import org.springframework.beans.factory.annotation.Value;

/**
 * User: schambon
 * Date: 10/17/14
 */
public class PortalOpenIdCConfiguration extends StaticOpenIdCConfiguration {

    @Value("${application.security.noauthdevmode:false}")
    private boolean noauthdevmode;
    @Value("${application.devmode:false}")
    private boolean devmode;

    @Override
    public boolean requireAuthenticationForPath(String path) {
        return path.contains("/my/api/" + ((noauthdevmode && devmode) ? "nothing" : ""))
            && !path.contains("/api/organization/import");
    }

    @Override
    public boolean skipAuthenticationForPath(String path) {
        return path.contains("/api/organization/import");
    }
}
