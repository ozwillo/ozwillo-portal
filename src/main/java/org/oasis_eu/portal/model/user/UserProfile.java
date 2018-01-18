package org.oasis_eu.portal.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.spring.kernel.model.Address;
import org.oasis_eu.spring.kernel.model.BaseUserInfo;

import java.io.Serializable;
import java.time.Instant;

/**
 * Data holder for the info returned by the Ozwillo kernel's user directory endpoint.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfile extends BaseUserInfo implements Serializable {
    private static final long serialVersionUID = 5630983084892826427L;

    @JsonProperty("id")
    private String userId;
    @JsonProperty("franceconnect_sub")
    private String franceConnectSub;
    @JsonProperty("email_address")
    private String email;
    @JsonProperty("email_verified")
    private Boolean emailVerified;
    @JsonProperty("created_at")
    private Long createdAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public Boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Instant getUpdateInstant() {
        return this.getUpdatedAt()!= null ? Instant.ofEpochSecond(this.getUpdatedAt()) : null;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getFranceConnectSub() {
        return franceConnectSub;
    }

    public void setFranceConnectSub(String franceConnectSub) {
        this.franceConnectSub = franceConnectSub;
    }

    public String getDisplayName() {
        if (getNickname() != null) return getNickname();
        if (getName() != null) return getName();
        if (getGivenName() != null && getFamilyName() != null)
            return String.format("%s %s", getGivenName(), getFamilyName());
        return getEmail() != null ? getEmail() : userId;
    }
}
