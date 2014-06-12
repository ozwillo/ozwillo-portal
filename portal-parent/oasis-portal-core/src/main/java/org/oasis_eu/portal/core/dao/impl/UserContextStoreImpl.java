package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.UserContextStore;
import org.oasis_eu.portal.core.model.subscription.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * User: schambon
 * Date: 6/12/14
 */
@Component
public class UserContextStoreImpl implements UserContextStore {

    @Autowired
    private RestTemplate kernelRestTemplate;

    private String userContextEndpoint = "http://localhost:8081/ctx";

    @Override
    public void deleteUserContext(String userId, String userContextId) {

    }

    @Override
    public UserContext updateUserContext(String userId, UserContext userContext) {
        return null;
    }

    @Override
    public UserContext getUserContext(String userId, String userContextId) {
        return null;
    }

    @Override
    public List<UserContext> getUserContexts(String userId) {

        URI uri = UriComponentsBuilder.fromUriString(userContextEndpoint)
                .path("/{user_id}")
                .buildAndExpand(userId)
                .encode()
                .toUri();

        UserContext[] ctxs = kernelRestTemplate.getForObject(uri, UserContext[].class);

        return Arrays.asList(ctxs);
    }

    @Override
    public UserContext addUserContext(String userId, UserContext userContext) {

        URI uri = UriComponentsBuilder.fromUriString(userContextEndpoint)
                .path("/{user_id}")
                .buildAndExpand(userId)
                .encode()
                .toUri();

        return kernelRestTemplate.postForObject(uri, userContext, UserContext.class);

    }
}
