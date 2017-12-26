package org.oasis_eu.portal.model.organization;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Pending membership of an organization
 *
 * User: ilucatero
 * Date: 05/28/15
 */
public class PendingOrgMembership {

	@JsonProperty
	private String id;

	@JsonProperty("pending_membership_uri")
	private String membershipUri;

	@JsonProperty("pending_membership_etag")
	private String membershipEtag;

	@JsonProperty
	private String email;

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
