package org.oasis_eu.portal.services;

import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Service
public class UserInfoHelper {

    public UserInfo currentUser() {

//        UserInfo dummy = new UserInfo();
//        dummy.setUserId("bb2c6f76-362f-46aa-982c-1fc60d54b8ef");
//        dummy.setName("Dummy Offline");
//
//        return dummy;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OpenIdCAuthentication) {
            return ((OpenIdCAuthentication) authentication).getUserInfo();
        } else {
            return null;
        }
    }
}
