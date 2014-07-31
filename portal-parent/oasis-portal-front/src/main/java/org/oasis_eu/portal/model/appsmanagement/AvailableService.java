package org.oasis_eu.portal.model.appsmanagement;

import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.spring.kernel.model.instance.ServiceCreated;

/**
 * User: schambon
 * Date: 7/29/14
 */
public class AvailableService {

    private CatalogEntry service;
    private SubscriptionStatus subscriptionStatus;

    public CatalogEntry getService() {
        return service;
    }

    public AvailableService setService(CatalogEntry service) {
        this.service = service;
        return this;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public AvailableService setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
        return this;
    }
}
