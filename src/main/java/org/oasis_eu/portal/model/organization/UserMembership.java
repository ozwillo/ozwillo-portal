package org.oasis_eu.portal.model.organization;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon
 * Date: 7/24/14
 */
public class UserMembership {

    @JsonProperty
    private String id;
    @JsonProperty("membership_uri")
    private String membershipUri;
    @JsonProperty("membership_etag")
    private String membershipEtag;
    @JsonProperty("organization_id")
    private String organizationId;
    @JsonProperty("organization_name")
    private String organizationName;
    @JsonProperty
    private boolean admin;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMembershipUri() {
        return membershipUri;
    }

    public void setMembershipUri(String membershipUri) {
        this.membershipUri = membershipUri;
    }

    public String getMembershipEtag() {
        return membershipEtag;
    }

    public void setMembershipEtag(String membershipEtag) {
        this.membershipEtag = membershipEtag;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
