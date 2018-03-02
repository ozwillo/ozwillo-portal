package org.oasis_eu.portal.model.app.service;

import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.subscription.Subscription;

import java.util.List;

/**
 * User: schambon
 * Date: 7/29/14
 */
public class InstanceService {

    private CatalogEntry catalogEntry;
    private SubscriptionStatus subscriptionStatus;

    private String name;

    private String iconUrl;

    private List<Subscription> subscriptions;

    public CatalogEntry getCatalogEntry() {
        return catalogEntry;
    }

    public InstanceService setCatalogEntry(CatalogEntry catalogEntry) {
        this.catalogEntry = catalogEntry;
        return this;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public InstanceService setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
        return this;
    }

    public String getName() {
        return name;
    }

    public InstanceService setName(String name) {
        this.name = name;
        return this;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public InstanceService setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public InstanceService setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
        return this;
    }
}
