package org.oasis_eu.portal.services;

import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.oasis_eu.spring.kernel.security.OpenIdCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SystemUserService {

    @Value("${datacore.systemAdminUser.refreshToken:GET IT USING NODE LIB}")
    private String refreshToken;
    @Value("${datacore.systemAdminUser.nonce:SET WHEN GETTING REFRESH TOKEN}")
    private String refreshTokenNonce;

    @Autowired
    private EnvPropertiesService envPropertiesService;

    /*@Value("${kernel.scopes_to_require: openid profile email address phone datacore}")
	private String scopesToRequire;*/ // see comment in loginAs()

    @Autowired
    private OpenIdCService openIdCService;

    public void runAs(Runnable runnable) {
        Authentication endUserAuth = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(null); // or UnauthAuth ?? anyway avoid to do next queries to Kernel with user auth
        try {
            loginAs();
            if (runnable != null) {
                runnable.run();
            }
        } finally {
            SecurityContextHolder.getContext().setAuthentication(endUserAuth);
        }
    }

    private void loginAs() {
	   /* 1. do query to Kernel "exchange refresh token for access token" (similar to "exchange code for acces token") see https://tools.ietf.org/html/rfc6749#section-6
		  2. wrap token in a Spring Authentication impl */
	    String callBackUri = envPropertiesService.getCurrentConfig().getKernel().getCallback_uri();

        String state = null, savedState = null;
        // Refresh_Token rather than Code
        // NB. In the Kernel auth spec says that the SCOPE is required, but actually if the scope is not added in the refresh token,
        // the original scope is taken by default. If the scope has new elements, then it will throw an exception. So is better not send it.
        OpenIdCAuthentication authentication = openIdCService.processAuthentication(
            null, refreshToken.trim(), state, savedState, refreshTokenNonce.trim(), callBackUri.trim());
        // set it as authenticated user for current context:
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
