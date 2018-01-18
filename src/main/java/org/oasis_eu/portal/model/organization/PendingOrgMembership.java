package org.oasis_eu.portal.model.organization;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Pending membership of an organization
 *
 * User: ilucatero
 * Date: 05/28/15
 */
public class PendingOrgMembership extends OrgMembership{
    @JsonProperty
	private String email;

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
