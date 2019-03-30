package org.oasis_eu.portal.services;

import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.oasis_eu.spring.kernel.security.OpenIdCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Service
public class SystemUserService {

    private final EnvPropertiesService envPropertiesService;

    private final OpenIdCService openIdCService;

    @Autowired
    public SystemUserService(EnvPropertiesService envPropertiesService, OpenIdCService openIdCService) {
        this.envPropertiesService = envPropertiesService;
        this.openIdCService = openIdCService;
    }

    public void runAs(@NotNull Runnable runnable) {
        Authentication endUserAuth = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(null);
        try {
            loginAs();
            runnable.run();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(endUserAuth);
        }
    }

    private void loginAs() {
        // 1. do query to Kernel "exchange refresh token for access token" (similar to "exchange code for acces token")
        //    see https://tools.ietf.org/html/rfc6749#section-6
        // 2. wrap token in a Spring Authentication impl

        EnvConfig defaultEnvConfig = envPropertiesService.getDefaultConfig();
	    String callBackUri = defaultEnvConfig.getKernel().getCallback_uri();

        OpenIdCAuthentication authentication = openIdCService.processAuthentication(
            null, defaultEnvConfig.getDatacore().getAdminUserRefreshToken().trim(), null, null,
                defaultEnvConfig.getDatacore().getNonce().trim(), callBackUri.trim());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
