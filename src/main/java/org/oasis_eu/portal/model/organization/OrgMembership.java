package org.oasis_eu.portal.model.organization;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Membership of an organization
 *
 * User: schambon
 * Date: 8/14/14
 */
public class OrgMembership {

    @JsonProperty
    private String id;

    @JsonProperty("membership_uri")
    private String membershipUri;

    @JsonProperty("membership_etag")
    private String membershipEtag;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("account_name")
    private String accountName;

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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
