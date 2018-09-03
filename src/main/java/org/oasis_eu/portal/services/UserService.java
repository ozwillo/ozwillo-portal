package org.oasis_eu.portal.services;

import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.oasis_eu.spring.kernel.security.OpenIdCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private OpenIdCService openIdCService;

    public boolean requiresLogout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OpenIdCAuthentication) {

            OpenIdCAuthentication openIdCAuthentication = (OpenIdCAuthentication) authentication;

            return openIdCService.getUserInfo(openIdCAuthentication) == null;
        }
        return false;
    }
}
