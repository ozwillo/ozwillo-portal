package org.oasis_eu.portal.model.appsmanagement;

import org.oasis_eu.portal.core.model.catalog.CatalogEntry;

/**
 * User: schambon
 * Date: 7/29/14
 */
public class MyAppsService {

    private CatalogEntry service;
    private SubscriptionStatus subscriptionStatus;

    private String name;

    public CatalogEntry getService() {
        return service;
    }

    public MyAppsService setService(CatalogEntry service) {
        this.service = service;
        return this;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public MyAppsService setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
        return this;
    }

    public String getName() {
        return name;
    }

    public MyAppsService setName(String name) {
        this.name = name;
        return this;
    }
}
