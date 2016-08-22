package org.oasis_eu.portal.model;

/**
 * User: schambon
 * Date: 6/18/14
 */
public class MyNavigation {

    String id;
    boolean active;

    public MyNavigation(String id, boolean active) {
        this.id = id;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
