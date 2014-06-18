package org.oasis_eu.portal.model;

/**
 * User: schambon
 * Date: 6/18/14
 */
public class MyNavigation {

    String id;
    boolean active;

    public String getId() {
        return id;
    }

    public MyNavigation setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public MyNavigation setActive(boolean active) {
        this.active = active;
        return this;
    }
}
