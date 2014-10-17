package org.oasis_eu.portal.config;

import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;

/**
 * User: schambon
 * Date: 10/17/14
 */
public class PortalOpenIdCConfiguration extends StaticOpenIdCConfiguration {
    @Override
    public boolean requireAuthenticationForPath(String path) {
        return path.contains("/api/");
    }
}
