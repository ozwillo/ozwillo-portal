package org.oasis_eu.portal.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon
 * Date: 10/24/14
 */
public class UIPendingOrganizationMember extends UIOrganizationMember {

    @JsonProperty("pendingMembershipUri")
    String pendingMembershipUri;
    @JsonProperty("pendingMembershipEtag")
    String pendingMembershipEtag;
    @JsonProperty
    String email;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPendingMembershipUri() {
        return pendingMembershipUri;
    }

    public void setPendingMembershipUri(String pendingMembershipUri) {
        this.pendingMembershipUri = pendingMembershipUri;
    }

    public String getPendingMembershipEtag() {
        return pendingMembershipEtag;
    }

    public void setPendingMembershipEtag(String pendingMembershipEtag) {
        this.pendingMembershipEtag = pendingMembershipEtag;
    }

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

    @Override
    public String toString() {
        return "{" +
            "id:'" + id + '\'' +
            ", pendingMembershipUri:'" + pendingMembershipUri + '\'' +
            ", pendingMembershipEtag:" + pendingMembershipEtag +
            ", email:" + email +
            ", admin:" + admin +
            '}';
    }
}
