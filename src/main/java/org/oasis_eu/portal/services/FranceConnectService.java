package org.oasis_eu.portal.services;

import org.oasis_eu.portal.model.kernel.UserProfile;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

@Service
public class FranceConnectService {

    @Autowired
    private Kernel kernel;

    @Value("${kernel.france_connect_endpoint}")
    private String franceConnectEndPoint;

    @Value("${kernel.user_profile_endpoint}")
    private String userProfileEndpoint;

    public UserProfile getFranceConnectInfo() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        //String uriString = franceConnectEndPoint + "/userinfo";
        String uriString = "https://kernel.ozwillo-dev.eu/a/franceconnect/userinfo";
        ResponseEntity<UserProfile> kernelResp = kernel.exchange(uriString, HttpMethod.GET, new HttpEntity<>(headers),
                UserProfile.class, user());

        UserProfile userProfile = kernel.getBodyUnlessClientError(kernelResp, UserProfile.class, uriString);
        return  userProfile;
    }

}
