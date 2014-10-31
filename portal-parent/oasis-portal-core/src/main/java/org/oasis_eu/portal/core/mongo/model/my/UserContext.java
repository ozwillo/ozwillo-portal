package org.oasis_eu.portal.core.mongo.model.my;

import java.util.ArrayList;
import java.util.List;

/**
 * User: schambon
 * Date: 6/12/14
 */
public class UserContext {

    private String id;
    private String name;
    private boolean primary = false;
    private List<UserSubscription> subscriptions = new ArrayList<>();

    public String getId() {
        return id;
    }

    public UserContext setId(String id) {
        this.id = id;
        return this;
    }

    public List<UserSubscription> getSubscriptions() {
        return subscriptions;
    }

    public UserContext setSubscriptions(List<UserSubscription> subscriptions) {
        this.subscriptions = subscriptions;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserContext setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isPrimary() {
        return primary;
    }

    public UserContext setPrimary(boolean primary) {
        this.primary = primary;
        return this;
    }
}
