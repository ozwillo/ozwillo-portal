package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.core.mongo.dao.store.InstalledStatusRepository;
import org.oasis_eu.portal.core.mongo.model.store.InstalledStatus;
import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Arrays;
import java.util.List;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Component
public class SubscriptionStoreImpl implements SubscriptionStore {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionStoreImpl.class);

    @Autowired
    private Kernel kernel;

    @Value("${kernel.portal_endpoints.subscriptions}")
    private String endpoint;

    @Autowired
    private InstalledStatusRepository installedStatusRepository;

    @Override
    @Cacheable("subscriptions")
    public List<Subscription> findByUserId(String userId) {
        try {
            ResponseEntity<Subscription[]> response = kernel.exchange(endpoint + "/user/{user_id}", HttpMethod.GET, null, Subscription[].class, user(), userId);

            if (logger.isDebugEnabled()) {
                logger.debug("--> " + response.getStatusCode());
                if (response.getBody() != null) {
                    for (Subscription s : response.getBody()) {
                        logger.debug("Subscribed: {}", s);
                    }
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
        logger.debug("Subscribing user {} to service {}", userId, subscription.getServiceId());

        InstalledStatus status = installedStatusRepository.findByCatalogEntryTypeAndCatalogEntryIdAndUserId(CatalogEntryType.SERVICE, subscription.getServiceId(), userId);
        if (status != null) {
            installedStatusRepository.delete(status);
        }

        ResponseEntity<Void> response = kernel.exchange(endpoint + "/user/{user_id}", HttpMethod.POST, new HttpEntity<>(subscription), Void.class, user(), userId);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.debug("Created subscription: {}", response.getStatusCode());
        } else {
            logger.warn("Subscription creation failed: {} {}", response.getStatusCode(), response.getStatusCode().getReasonPhrase());
            if (response.getStatusCode().is4xxClientError()) {
                throw new WrongQueryException();
            } else {
                throw new TechnicalErrorException();
            }
        }
    }


    @Override
    public List<Subscription> findByServiceId(String serviceId) {
        ResponseEntity<Subscription[]> response = kernel.exchange(endpoint + "/service/{service_id}", HttpMethod.GET, null, Subscription[].class, user(), serviceId);

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

        logger.debug("Unsubscribing user {} from service {}", userId, serviceId);

        InstalledStatus status = installedStatusRepository.findByCatalogEntryTypeAndCatalogEntryIdAndUserId(CatalogEntryType.SERVICE, serviceId, userId);
        if (status != null) {
            installedStatusRepository.delete(status);
        }


        List<Subscription> subs = findByUserId(userId);


        subs.stream().filter(s -> s.getServiceId().equals(serviceId) && s.getSubscriptionType().equals(subscriptionType))
                .forEach(s -> { // forEach... but there should only be one...
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("If-Match", s.getSubscriptionEtag());

                    kernel.exchange(endpoint + "/subscription/{subscription_id}", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class, user(), s.getId());
                });

    }
}
