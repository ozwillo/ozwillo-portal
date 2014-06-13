package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.subscription.Subscription;
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
 * Date: 6/13/14
 */
@Component
public class SubscriptionStoreImpl implements SubscriptionStore {

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Value("${kernel.portal_endpoints.subscriptions}")
    private String endpoint;

    @Override
    public List<Subscription> findByUserIdAndUserContextId(String userId, String userContextId) {

        return Arrays.asList(kernelRestTemplate.getForObject(endpoint + "/{user_id}/{context_id}", Subscription[].class, userId, userContextId));
    }

    @Override
    public List<Subscription> findByUserIdForPrimaryContext(String userId) {
        return Arrays.asList(kernelRestTemplate.getForObject(endpoint + "/{user_id}", Subscription[].class, userId));
    }

    @Override
    public Subscription create(Subscription subscription) {
        return null;
    }

    @Override
    public void update(Subscription subscription) {

    }

    @Override
    public void delete(Subscription subscription) {

    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public Subscription find(String id) {
        return null;
    }

    @Override
    public List<Subscription> find() {
        return null;
    }

    @Override
    public List<Subscription> find(int skip, int number) {
        return null;
    }
}
