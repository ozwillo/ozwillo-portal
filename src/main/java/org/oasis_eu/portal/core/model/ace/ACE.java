package org.oasis_eu.portal.core.model.ace;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: schambon
 * Date: 9/16/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ACE {

	private static final Logger logger = LoggerFactory.getLogger(ACE.class);

	private String id;
	@JsonProperty("entry_uri")
	private String entryUri;
	@JsonProperty("entry_etag")
	private String entryEtag;
	@JsonProperty("instance_id")
	private String instanceId;
	@JsonProperty("user_id")
	private String userId;
	@JsonProperty("user_name")
	private String userName;
	@JsonProperty("email")
	private String email;
	@JsonProperty("creator_id")
	private String creatorId;
	@JsonProperty("creator_name")
	private String creatorName;
	@JsonProperty("app_admin")
	private boolean appAdmin = false; // for now Kernel deduces it from orga admin #157 Delete and re-add a service icon to my desk K#90
	@JsonProperty("app_user")
	private boolean appUser = false; // #157 Delete and re-add a service icon to my desk K#90

	@JsonAnySetter
	public void anySetter(String key, String value) {
		logger.debug("Unmarshalling ACE, discarding unknown key {} with value {}", key, value);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEntryUri() {
		return entryUri;
	}

	public void setEntryUri(String entryUri) {
		this.entryUri = entryUri;
	}

	public String getEntryEtag() {
		return entryEtag;
	}

	public void setEntryEtag(String entryEtag) {
		this.entryEtag = entryEtag;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public boolean isAppAdmin() {
		return appAdmin;
	}

	public void setAppAdmin(boolean appAdmin) {
		this.appAdmin = appAdmin;
	}

	public boolean isAppUser() {
		return appUser;
	}

	public void setAppUser(boolean appUser) {
		this.appUser = appUser;
	}
}
