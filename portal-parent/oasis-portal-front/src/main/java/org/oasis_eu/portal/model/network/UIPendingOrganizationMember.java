package org.oasis_eu.portal.model.network;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon
 * Date: 10/24/14
 */
public class UIPendingOrganizationMember {
	@JsonProperty String id;
	@JsonProperty String pending_membership_uri;
	@JsonProperty String pending_membership_etag;
	@JsonProperty String email;
	@JsonProperty boolean admin;
	
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPending_membership_uri() {
		return pending_membership_uri;
	}

	public void setPending_membership_uri(String pending_membership_uri) {
		this.pending_membership_uri = pending_membership_uri;
	}

	public String getPending_membership_etag() {
		return pending_membership_etag;
	}

	public void setPending_membership_etag(String pending_membership_etag) {
		this.pending_membership_etag = pending_membership_etag;
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
                ", pending_membership_uri:'" + pending_membership_uri + '\'' +
                ", pending_membership_etag:" + pending_membership_etag +
                ", email:" + email +
                ", admin:" + admin +
                '}';
    }
}
