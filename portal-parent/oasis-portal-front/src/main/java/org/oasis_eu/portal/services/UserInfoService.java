package org.oasis_eu.portal.services;

import java.io.Serializable;
import java.util.Map;

import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.oasis_eu.spring.kernel.security.OpenIdCService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Service
public class UserInfoService {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);

    @Autowired
    private OpenIdCService openIdCService;

    public UserInfo currentUser() {
    	OpenIdCAuthentication authentication = getOpenIdCAuthentication();
        if (authentication != null) {
            return authentication.getUserInfo();
        } else {
            return null;
        }
    }
    
    public void saveUserInfo(Map<String, Serializable> userProperties) {
    	OpenIdCAuthentication authentication = getOpenIdCAuthentication();
        if (authentication != null) {
            openIdCService.saveUserInfo(authentication.getAccessToken(), userProperties);
            refreshCurrentUser();
        }
    }
    
    private void refreshCurrentUser() {
    	OpenIdCAuthentication authentication = getOpenIdCAuthentication();
        if (authentication != null) {
            UserInfo userInfo = openIdCService.getUserInfo(authentication.getAccessToken());
            authentication.setUserInfo(userInfo);
        }
    }
    
    private OpenIdCAuthentication getOpenIdCAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OpenIdCAuthentication) {
            return (OpenIdCAuthentication) authentication;
        } else {
            return null;
        }
    }
}
