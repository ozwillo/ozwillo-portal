package org.oasis_eu.portal.core.model.ace;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.oasis_eu.portal.config.CustomInstantSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

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
    @JsonProperty("pending_entry_uri")
    private String pendingEntryUri;
    @JsonProperty("pending_entry_etag")
    private String pendingEntryEtag;
    @JsonProperty("instance_id")
    private String instanceId;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("user_name")
    private String userName;

    //TODO: standardize name of parameters. To fetch an ACL we receive a field "user_email_address" (see ACE class) but to create an ACL we need a field "email"
    private String email;

    @JsonProperty("user_email_address")
    private String userEmail;

    @JsonProperty("creator_id")
    private String creatorId;
    @JsonProperty("creator_name")
    private String creatorName;
    @JsonProperty("created")
    @JsonSerialize(using = CustomInstantSerializer.class)
    private Instant created;
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

    public String getPendingEntryEtag() {
        return pendingEntryEtag;
    }

    public void setPendingEntryEtag(String pendingEntryEtag) {
        this.pendingEntryEtag = pendingEntryEtag;
    }

    public String getPendingEntryUri() {
        return pendingEntryUri;
    }

    public void setPendingEntryUri(String pendingEntryUri) {
        this.pendingEntryUri = pendingEntryUri;
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

    //TODO: standardize name of parameters. (Quick fix)
    public String getEmail() {
        return (email == null || email.isEmpty()) ? email : userEmail;
    }

    public void setEmail(String email) {
        this.email = email;
        this.userEmail = email;
    }

    public String getUserEmail() {
        return getEmail();
    }

    public void setUserEmail(String userEmail) {
        setEmail(userEmail);
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

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
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
