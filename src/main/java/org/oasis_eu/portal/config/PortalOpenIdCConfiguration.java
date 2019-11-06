package org.oasis_eu.portal.config;

import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.oasis_eu.portal.services.EnvPropertiesService;
import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;

public class PortalOpenIdCConfiguration extends StaticOpenIdCConfiguration {

    @Value("${application.security.noauthdevmode:false}")
    private boolean noauthdevmode;
    @Value("${application.devmode:false}")
    private boolean devmode;

    @Autowired
    private EnvPropertiesService envPropertiesService;

    @Override
    public boolean requireAuthenticationForPath(String path) {
        return path.contains("/my/api/" + ((noauthdevmode && devmode) ? "nothing" : ""))
            && !(path.contains("/api/organization/import") || path.contains("/status"));
    }

    @Override
    public boolean skipAuthenticationForPath(String path) {
        return path.contains("/api/organization/import") || path.contains("/status")
                || path.contains("/media") || path.startsWith("/actuator");
    }

    @Override
    public String getClientId() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            EnvConfig envConfig = this.envPropertiesService.getCurrentConfig();
            if (envConfig != null) {
                return envConfig.getKernel().getClient_id();
            } else {
                return "invalid";
            }
        } else {
            return envPropertiesService.getDefaultConfig().getKernel().getClient_id();
        }
    }

    @Override
    public String getClientSecret() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            EnvConfig envConfig = this.envPropertiesService.getCurrentConfig();
            if (envConfig != null) {
                return envConfig.getKernel().getClient_secret();
            } else {
                return "invalid";
            }
        } else {
            return envPropertiesService.getDefaultConfig().getKernel().getClient_secret();
        }
    }

    @Override
    public String getCallbackUri() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            EnvConfig envConfig = this.envPropertiesService.getCurrentConfig();
            if (envConfig != null) {
                return envConfig.getKernel().getCallback_uri();
            } else {
                return "invalid";
            }
        } else {
            return envPropertiesService.getDefaultConfig().getKernel().getCallback_uri();
        }
    }

    @Override
    public String getPostLogoutRedirectUri() {
        EnvConfig envConfig = this.envPropertiesService.getCurrentConfig();
        if (envConfig != null) {
            return envConfig.getKernel().getPost_logout_redirect_uri();
        }
        return null;
    }

    @Override
    public String getHomeUri() {
        EnvConfig envConfig = this.envPropertiesService.getCurrentConfig();
        if (envConfig != null) {
            return envConfig.getKernel().getHome_uri();
        }
        return null;
    }

    @Override
    public String getError401Uri() {
        EnvConfig envConfig = this.envPropertiesService.getCurrentConfig();
        if (envConfig != null) {
            return envConfig.getKernel().getError_401_uri();
        }
        return null;
    }
}
