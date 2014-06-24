package org.oasis_eu.portal.model;

/**
 * User: schambon
 * Date: 6/18/14
 */
public class BackendNavigation {

    String id;
    boolean active;

    public String getId() {
        return id;
    }

    public BackendNavigation setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public BackendNavigation setActive(boolean active) {
        this.active = active;
        return this;
    }
}
