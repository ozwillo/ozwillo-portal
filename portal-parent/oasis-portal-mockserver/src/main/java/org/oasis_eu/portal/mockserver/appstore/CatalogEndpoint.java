package org.oasis_eu.portal.mockserver.appstore;

import org.joda.time.Instant;
import org.oasis_eu.portal.core.model.appstore.Audience;
import org.oasis_eu.portal.core.model.appstore.CatalogEntry;
import org.oasis_eu.portal.core.model.appstore.CatalogEntryType;
import org.oasis_eu.portal.core.model.appstore.PaymentOption;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.mockserver.repo.Catalog;
import org.oasis_eu.portal.mockserver.repo.PendingCreationRequests;
import org.oasis_eu.portal.mockserver.repo.Subscriptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * User: schambon
 * Date: 6/24/14
 */

@RestController
@RequestMapping("/catalog")
@Deprecated
public class CatalogEndpoint {

//    @Autowired
//    private Catalog catalog;

    @Autowired
    private Subscriptions subscriptions;

    @Autowired
    private PendingCreationRequests pendingCreationRequests;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(method = POST, value="/testdata")
    public void resetTestData() {

//        catalog.deleteAll();
        subscriptions.deleteAll();

//        CatalogEntry ckArchetype = new CatalogEntry();
//        ckArchetype.setType(CatalogEntryType.APPLICATION);
//        ckArchetype.setId("__citizenkin__");
//        ckArchetype.setDefaultName("Citizen Kin");
//        ckArchetype.setDefaultDescription("Citizen relationship management application");
//        ckArchetype.setLocalizedDescriptions(new HashMap<String, String>() {{
//            put("fr", "Application de gestion de la relation usager");
//        }});
//        ckArchetype.setTargetAudience(Arrays.asList(Audience.PUBLIC_BODIES));
//        ckArchetype.setPaymentOption(PaymentOption.PAID);
//        ckArchetype.setVisible(true);
//        ckArchetype.setProviderId("6dccdb8d-ec46-4675-9965-806ea37b73e1");      // Open Wide
//        ckArchetype.setInstantiationEndpoint("http://localhost:9090/admin/create-instance");
//        ckArchetype.setSecret("--secret citizen kin password--");
//
//        catalog.save(ckArchetype);
    }


//    @RequestMapping(method = GET, value = "/{id}")
//    public CatalogEntry find(@PathVariable String id) {
//        return catalog.findOne(id);
//    }
//
//    @RequestMapping(method = GET, value = "")
//    public List<CatalogEntry> findAllVisible(@RequestParam List<Audience> targetAudience) {
//        return catalog.findByVisibleAndTargetAudienceIn(true, targetAudience);
//    }

//    @RequestMapping(method = POST, value = "/buy/{id}/{userId}")
//    public void subscribe(@PathVariable String userId, @PathVariable String id) {
//
//        CatalogEntry base = catalog.findOne(id);
//        if (base != null) {
//
//            if (base.getType().equals(CatalogEntryType.SERVICE)) {
//                // easy case: just add a subscription to this very service
//                Subscription s = new Subscription();
//                s.setSubscriptionType(SubscriptionType.PERSONAL);
//                s.setId(UUID.randomUUID().toString());
//                s.setCreated(Instant.now());
//                s.setServiceId(id);
//                s.setUserId(userId);
//
//                subscriptions.save(s);
//                return;
//
//            } else {
//
//                CreateInstanceRequest request = new CreateInstanceRequest();
//                request.setClientId("41184194-d40b-4720-87a9-284d2fa9d5ed"); // Citizen Kin
//                request.setClientSecret("41184194-d40b-4720-87a9-284d2fa9d5ed");
//                request.setInstanceId(UUID.randomUUID().toString());
//                request.setOrganizationId("a2342900-f9eb-4d54-bf30-1e0d763ec4af");  // Valence
//                request.setUserId(userId);
//
//                pendingCreationRequests.save(request);
//
//                HttpHeaders headers = new HttpHeaders();
//                headers.add("X-OASIS-Signature", "** HMAC digest of payload **");
//                HttpEntity<CreateInstanceRequest> req = new HttpEntity<>(request, headers);
//
//                ResponseEntity<Void> instance = restTemplate.exchange(base.getInstantiationEndpoint(), HttpMethod.POST, req, Void.class);
//
//                if (! instance.getStatusCode().is2xxSuccessful()) {
//                    throw new UnableToInstantiateException();
//                }
//
//
//            }
//
//        } else {
//            throw new NotFoundException();
//        }
//
//    }


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private void handleException() {}

    @ExceptionHandler(UnableToInstantiateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private void handleUnableToInstantiate() {}

    private static class UnableToInstantiateException extends RuntimeException {
    }

    private static class NotFoundException extends RuntimeException {
    }
}

