package org.oasis_eu.portal.services.kernel;

import org.oasis_eu.portal.model.user.UserProfile;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.oasis_eu.spring.kernel.security.OpenIdCService;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

@Service
public class UserProfileService {

    @Autowired
    private Kernel kernel;

    @Autowired
    private OpenIdCService openIdCService;

    @Value("${kernel.user_profile_endpoint}")
    private String userProfileEndpoint;

    @CacheEvict(value = "accounts", key = "#userProfile.userId")
    public void saveUserProfile(UserProfile userProfile) throws WrongQueryException {

        ResponseEntity<UserProfile> response = kernel.exchange(userProfileEndpoint + "/{userId}",
            HttpMethod.GET, null, UserProfile.class, user(), userProfile.getUserId());

        String etag = response.getHeaders().getETag();

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", etag);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String uriString = userProfileEndpoint + "/{userId}";
        ResponseEntity<UserProfile> kernelResp = kernel.exchange(uriString, HttpMethod.PUT, new HttpEntity<Object>(userProfile, headers),
            UserProfile.class, user(), userProfile.getUserId());
        // validate response body
        kernel.getBodyUnlessClientError(kernelResp, UserProfile.class, uriString);

        refreshCurrentUser();
    }

    @Cacheable("accounts")
    public UserProfile findUserProfile(String id) {
        return kernel.getEntityOrException(userProfileEndpoint + "/{userId}", UserProfile.class, user(), id);
    }

    private void refreshCurrentUser() {
        OpenIdCAuthentication authentication = getOpenIdCAuthentication();
        if (authentication != null) {
            UserInfo userInfo = openIdCService.getUserInfo(authentication);
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
