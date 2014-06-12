package org.oasis_eu.portal.core.mongo.model.my;

import org.oasis_eu.portal.core.model.appstore.GenericEntity;

/**
 * User: schambon
 * Date: 6/12/14
 */
public class DashboardOrdering extends GenericEntity {

    private String userId;
    private String subscriptionId;
    private int ordering;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }
}
