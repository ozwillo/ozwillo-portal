package org.oasis_eu.portal.mockserver.provisioning;

import org.joda.time.Instant;
import org.oasis_eu.portal.core.model.appstore.CatalogEntry;
import org.oasis_eu.portal.core.model.appstore.CatalogEntryType;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.mockserver.appstore.CreateInstanceRequest;
import org.oasis_eu.portal.mockserver.repo.Catalog;
import org.oasis_eu.portal.mockserver.repo.PendingCreationRequests;
import org.oasis_eu.portal.mockserver.repo.Subscriptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 7/2/14
 */
@RestController
public class ProvisioningEndpoint {

    @Autowired
    private Catalog catalog;

    @Autowired
    private PendingCreationRequests pendingCreationRequests;

    @Autowired
    private Subscriptions subscriptions;


    @RequestMapping(method = RequestMethod.POST, value = "/instance-created")
    public Map<String, String> acquitInstanceCreated(@RequestBody InstanceCreated instanceCreated) {

        Map<String, String> result = new HashMap<>();

        CreateInstanceRequest request = pendingCreationRequests.findOne(instanceCreated.getInstanceId());

        instanceCreated.getServices().forEach(s -> {
            CatalogEntry entry = new CatalogEntry();
            entry.setId(UUID.randomUUID().toString());
            entry.setType(CatalogEntryType.SERVICE);
            entry.setParentId(instanceCreated.getInstanceId());
            entry.setProviderId(s.getProviderId());
            entry.setDefaultDescription(s.getDefaultDescription());
            entry.setDefaultName(s.getDefaultName());
            entry.setUrl(s.getUrl());
            entry.setNotificationUrl(s.getNotificationUrl());
            entry.setLocalizedDescriptions(s.getTranslations().entrySet().stream().filter(e -> e.getKey().startsWith("description#")).collect(Collectors.toMap(e -> e.getKey().substring("description#".length()), Map.Entry::getValue)));
            entry.setLocalizedNames(s.getTranslations().entrySet().stream().filter(e -> e.getKey().startsWith("name#")).collect(Collectors.toMap(e -> e.getKey().substring("name#".length()), Map.Entry::getValue)));
            entry.setVisible(s.isVisible());
            entry.setDefaultLocale(Locale.ENGLISH);
            entry.setPaymentOption(s.getPaymentOption());
            entry.setTargetAudience(s.getTargetAudience());

            catalog.save(entry);

            if (!entry.isVisible()) {
                Subscription sub = new Subscription();
                sub.setCreated(Instant.now());
                sub.setSubscriptionType(SubscriptionType.MANAGER);
                sub.setCatalogId(entry.getId());
                sub.setUserId(request.getUserId());

                subscriptions.save(sub);
            }

            result.put(s.getIdentifier(), entry.getId());
        });

        return result;

    }

}
