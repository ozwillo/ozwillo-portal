package org.oasis_eu.portal.model.organization;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class InvitationRequest {
    @JsonProperty
    @NotNull
    @NotEmpty
    String email;

    @JsonProperty
    boolean admin;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}