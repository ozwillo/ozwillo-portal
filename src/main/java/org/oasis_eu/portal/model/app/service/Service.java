package org.oasis_eu.portal.model.app.service;

import org.oasis_eu.portal.core.model.catalog.CatalogEntry;

/**
 * User: schambon
 * Date: 7/29/14
 */
public class Service {

    private CatalogEntry catalogEntry;
    private SubscriptionStatus subscriptionStatus;

    private String name;

    private String iconUrl;

    public CatalogEntry getCatalogEntry() {
        return catalogEntry;
    }

    public Service setCatalogEntry(CatalogEntry catalogEntry) {
        this.catalogEntry = catalogEntry;
        return this;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public Service setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
        return this;
    }

    public String getName() {
        return name;
    }

    public Service setName(String name) {
        this.name = name;
        return this;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public Service setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }
}
