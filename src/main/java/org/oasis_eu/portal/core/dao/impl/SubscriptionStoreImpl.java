package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.core.mongo.dao.store.InstalledStatusRepository;
import org.oasis_eu.portal.core.mongo.model.store.InstalledStatus;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
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

    @Value("${kernel.portal_endpoints.subscriptions}") // .../apps/subscriptions
    private String endpoint;

    @Autowired
    private InstalledStatusRepository installedStatusRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private HttpServletRequest request;

    @Override
    @Cacheable("subscriptions")
    public List<Subscription> findByUserId(String userId) {

        Subscription[] subscriptions = kernel.getEntityOrException(endpoint + "/user/{user_id}", Subscription[].class, user(), userId);

        if (logger.isDebugEnabled() && subscriptions != null) {
            for (Subscription s : subscriptions) {
                logger.debug("Subscribed: {}", s);
            }
        }

        return Arrays.asList(subscriptions);
    }

    @Override
    public Subscription create(String userId, Subscription subscription) throws WrongQueryException {

        logger.debug("Subscribing user {} to service {}", userId, subscription.getServiceId());

        InstalledStatus status = installedStatusRepository.findByCatalogEntryTypeAndCatalogEntryIdAndUserId(
            CatalogEntryType.SERVICE, subscription.getServiceId(), userId);

        if (status != null) {
            installedStatusRepository.delete(status);
        }

        Subscription newSub;
        try {
            String uri = endpoint + "/user/{user_id}";
            ResponseEntity<Subscription> kernelResp = kernel.exchange(uri, HttpMethod.POST,
                    new HttpEntity<>(subscription), Subscription.class, user(), userId);

            // validate response body
            newSub =  kernel.getBodyUnlessClientError(kernelResp, Subscription.class, uri);
        } catch(RestClientException e) { //TODO: Trigger another exception like WrongQueryException to check HttpStatus
            String translatedBusinessMessage = messageSource.getMessage("error.msg.user-is-already-subscribed",
                    new Object[]{}, RequestContextUtils.getLocale(request));

            throw new WrongQueryException(translatedBusinessMessage, HttpStatus.BAD_REQUEST.value());
        }

        return newSub;

    }


    @Override
    public List<Subscription> findByServiceId(String serviceId) {
        return Arrays.asList(kernel.getEntityOrException(endpoint + "/service/{service_id}", Subscription[].class, user(), serviceId));
    }

    @Override
    public void unsubscribe(String userId, String serviceId, SubscriptionType subscriptionType) {

        logger.debug("Unsubscribing user {} from service {}", userId, serviceId);

        InstalledStatus status = installedStatusRepository.findByCatalogEntryTypeAndCatalogEntryIdAndUserId(CatalogEntryType.SERVICE, serviceId, userId);
        if (status != null) {
            installedStatusRepository.delete(status);
        }

        List<Subscription> subs = findByServiceId(serviceId); // and NOT findByUserId() else 403 Forbidden for an other user than oneself even if admin
        subs.stream()
            .filter(s -> s.getSubscriptionType().equals(subscriptionType)
                && s.getUserId().equals(userId)) // NB. s.getServiceId() is obligatorily right (even if it is actually null !)
            .forEach(s -> { // forEach... but there should only be one...
                HttpHeaders headers = new HttpHeaders();
                headers.add("If-Match", s.getSubscriptionEtag());

                kernel.exchange(endpoint + "/subscription/{subscription_id}", HttpMethod.DELETE, new HttpEntity<>(headers),
                    Void.class, user(), s.getId());
            });

    }


    @Override
    public void unsubscribe(String subscriptionId) throws WrongQueryException {

        String uri = endpoint + "/subscription/{subscription_id}";
        ResponseEntity<Subscription> response = kernel.exchange(uri, HttpMethod.GET, null, Subscription.class, user(), subscriptionId);

        Subscription subscription = kernel.getBodyUnlessClientError(response, Subscription.class, uri, subscriptionId);

        InstalledStatus status = installedStatusRepository.findByCatalogEntryTypeAndCatalogEntryIdAndUserId(
            CatalogEntryType.SERVICE, subscription.getServiceId(), subscription.getUserId());
        if (status != null) {
            installedStatusRepository.delete(status);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", response.getHeaders().getETag());

        ResponseEntity<Subscription> kernelResp = kernel.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(headers),
            Subscription.class, user(), subscriptionId);
        // validate response body
        kernel.getBodyUnlessClientError(kernelResp, Subscription.class, uri); // TODO test
    }

}
