package org.oasis_eu.portal.model.network;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon
 * Date: 10/24/14
 */
public class UIOrganizationMember {
    @JsonProperty
    String id;
    @JsonProperty
    String name;
    @JsonProperty
    boolean admin;
    @JsonProperty
    boolean self;

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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    @Override
    public String toString() {
        return "{" +
                "id:'" + id + '\'' +
                ", name:'" + name + '\'' +
                ", admin:" + admin +
                ", self:" + self +
                '}';
    }
}
