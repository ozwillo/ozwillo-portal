package org.oasis_eu.portal.services.kernel;

import org.oasis_eu.portal.model.kernel.user.UserProfile;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

@Service
public class FranceConnectService {

    @Autowired
    private Kernel kernel;

    @Value("${kernel.france_connect.userinfo_endpoint}")
    private String userProfileEndpoint;

    public UserProfile getFranceConnectInfo() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<UserProfile> kernelResp = kernel.exchange(userProfileEndpoint, HttpMethod.GET, new HttpEntity<>(headers),
                UserProfile.class, user());

        return kernel.getBodyUnlessClientError(kernelResp, UserProfile.class, userProfileEndpoint);
    }
}
