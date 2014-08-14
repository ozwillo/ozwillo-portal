package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
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

        try {
            ResponseEntity<Subscription[]> response = kernelRestTemplate.exchange(endpoint + "/user/{user_id}", HttpMethod.GET, new HttpEntity<>(headers), Subscription[].class, userId);

            if (logger.isDebugEnabled()) {
                logger.debug("--> " + response.getStatusCode());
                for (Subscription s : response.getBody()) {
                    logger.debug("Subscribed: {}", s);

                }
            }

            return Arrays.asList(response.getBody());
        } catch (HttpServerErrorException hse) {
            logger.error("Remote server threw a 500", hse);
            throw new TechnicalErrorException();
        }
    }

    @Override
    public void create(String userId, Subscription subscription) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) SecurityContextHolder.getContext().getAuthentication()).getAccessToken()));

        ResponseEntity<Void> response = kernelRestTemplate.exchange(endpoint + "/user/{user_id}", HttpMethod.POST, new HttpEntity<>(subscription, headers), Void.class, userId);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.debug("Created subscription: {}", response.getStatusCode());

//            return response.getBody();
        } else {
            logger.warn("Subscription creation failed: {} {}", response.getStatusCode(), response.getStatusCode().getReasonPhrase());
            if (response.getStatusCode().is4xxClientError()) {
                throw new WrongQueryException();
            } else {
                throw new TechnicalErrorException();
            }
        }

//        return null;
    }


    @Override
    public List<Subscription> findByServiceId(String serviceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) SecurityContextHolder.getContext().getAuthentication()).getAccessToken()));

        ResponseEntity<Subscription[]> response = kernelRestTemplate.exchange(endpoint + "/service/{service_id}", HttpMethod.GET, new HttpEntity<Object>(headers), Subscription[].class, serviceId);

        if (response.getStatusCode().is2xxSuccessful()) {
            return Arrays.asList(response.getBody());
        } else {
            if (response.getStatusCode().is4xxClientError()) {
                throw new WrongQueryException();
            } else {
                throw new TechnicalErrorException();
            }
        }

    }

    @Override
    public void unsubscribe(String userId, String serviceId, SubscriptionType subscriptionType) {

        List<Subscription> subs = findByUserId(userId);


        subs.stream().filter(s -> s.getServiceId().equals(serviceId) && s.getSubscriptionType().equals(subscriptionType))
                .forEach(s -> { // forEach... but there should only be one...
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) SecurityContextHolder.getContext().getAuthentication()).getAccessToken()));
                    headers.add("If-Match", s.getSubscriptionEtag());

                    kernelRestTemplate.exchange(endpoint + "/subscription/{subscription_id}", HttpMethod.DELETE, new HttpEntity<Object>(headers), Void.class, s.getId());
                });

    }
}
