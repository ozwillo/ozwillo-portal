package org.oasis_eu.portal.model.kernel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.spring.kernel.model.Address;
import org.oasis_eu.spring.kernel.model.BaseUserInfo;

import java.io.Serializable;
import java.time.Instant;

/**
 * Data holder for the info returned by the Ozwillo kernel's user directory endpoint.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class UserProfile extends BaseUserInfo implements Serializable {
    private static final long serialVersionUID = 5630983084892826427L;

    @JsonProperty("id")
    private String userId;
    @JsonProperty("email_address")
    private String email;
    @JsonProperty("email_verified")
    private Boolean emailVerified;
    @JsonProperty("created_at")
    private Long createdAt;

    public static UserProfile from(UserProfile in) {
        UserProfile out = new UserProfile();
        out.setName(in.getName());
        out.setNickname(in.getNickname());
        out.setLocale(in.getLocale());
        out.setEmail(in.getEmail());
        out.setEmailVerified(in.isEmailVerified());
        out.setUserId(in.getUserId());
        out.setAddress(in.getAddress()); // mmm, it'd be better to deepclone, but let's not worry too much right now (we'll fix it when we see weird bugs)
        out.setBirthdate(in.getBirthdate());
        out.setFamilyName(in.getFamilyName());
        out.setGivenName(in.getGivenName());
        out.setGender(in.getGender());
        out.setPhoneNumber(in.getPhoneNumber());
        out.setPhoneNumberVerified(in.isPhoneNumberVerified() != null ? in.isPhoneNumberVerified() : false);
        out.setPictureUrl(in.getPictureUrl());
        out.setUpdatedAt(in.getUpdatedAt());
        out.setZoneInfo(in.getZoneInfo());

        return out;
    }

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

    /*
    public String getStreetAddress() {
        return this.getAddress() != null ? this.getAddress().getStreetAddress() : null;
    }


    public String getLocality() {
        return this.getAddress() != null ? this.getAddress().getLocality() : null;
    }

    public String getRegion() {
        return this.getAddress() != null ? this.getAddress().getRegion():null;
    }

    public String getPostalCode() {
        return this.getAddress()!=null ? this.getAddress().getPostalCode() : null;
    }

    public String getCountry() {
        return this.getAddress() != null ? this.getAddress().getCountry() : null;
    }
    */
    public Instant getUpdateInstant() {
        return this.getUpdatedAt()!= null ? Instant.ofEpochSecond(this.getUpdatedAt()) : null;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getDisplayName() {
        if (getNickname() != null) return getNickname();
        if (getName() != null) return getName();
        if (getGivenName() != null && getFamilyName() != null)
            return String.format("%s %s", getGivenName(), getFamilyName());
        return getEmail() != null ? getEmail() : userId;
    }
}
