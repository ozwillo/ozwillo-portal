package org.oasis_eu.portal.model.kernel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Membership of an organization
 *
 * User: ilucatero
 * Date: 05/28/15
 */
public class PendingOrgMembership {

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

}
