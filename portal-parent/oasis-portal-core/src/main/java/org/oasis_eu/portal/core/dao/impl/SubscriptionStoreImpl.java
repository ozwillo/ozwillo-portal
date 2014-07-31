package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Component
public class SubscriptionStoreImpl implements SubscriptionStore {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionStoreImpl.class);

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Value("${kernel.portal_endpoints.subscriptions}")
    private String endpoint;

    @Override
    public List<Subscription> findByUserId(String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) SecurityContextHolder.getContext().getAuthentication()).getAccessToken()));

        ResponseEntity<Subscription[]> response = kernelRestTemplate.exchange(endpoint + "/user/{user_id}", HttpMethod.GET, new HttpEntity<>(headers), Subscription[].class, userId);

        if (logger.isDebugEnabled()) {
            logger.debug("--> " + response.getStatusCode());
            for (Subscription s : response.getBody()) {
                logger.debug("Subscribed: {}", s);

            }
        }

        return Arrays.asList(response.getBody());
    }

    @Override
    public Subscription create(String userId, Subscription subscription) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) SecurityContextHolder.getContext().getAuthentication()).getAccessToken()));

        ResponseEntity<Subscription> response = kernelRestTemplate.exchange(endpoint + "/user/{user_id}", HttpMethod.POST, new HttpEntity<>(subscription, headers), Subscription.class, userId);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.debug("Created subscription: {}", response.getBody());

            return response.getBody();
        } else {
            logger.warn("Subscription creation failed: {} {}", response.getStatusCode(), response.getStatusCode().getReasonPhrase());
        }

        // TODO handle errors
        return null;
    }


}
