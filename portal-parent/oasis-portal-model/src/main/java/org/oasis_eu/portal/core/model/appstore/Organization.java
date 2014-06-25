package org.oasis_eu.portal.core.model.appstore;

import org.joda.time.Instant;

/**
 * User: schambon
 * Date: 6/25/14
 */
public class Organization {

    String id;
    String name;
    Instant modified;

    public Instant getModified() {
        return modified;
    }

    public void setModified(Instant modified) {
        this.modified = modified;
    }

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
}
