package org.oasis_eu.portal.ui;

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
    String email;
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

    /**
     * BEWARE may be null with old accounts before user nickname was required #171
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return name or, if null, id ; so NetworkService.toUIOrganization() can order old accounts
     * before nickname was required #171
     */
    public String getNonNullName() {
        return name == null ? id : name;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
