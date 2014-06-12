package org.oasis_eu.portal.core.model.subscription;

/**
 * User: schambon
 * Date: 6/12/14
 */
public class UserContext {

    private String id;
    private String name;
    private boolean primary;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
