package org.oasis_eu.portal.model.user;

import org.oasis_eu.portal.model.kernel.user.UserProfile;

import java.util.List;

public class UIUserProfile {

    private UserProfile userProfile;
    private UserProfile franceConnectProfile;
    private List<String> languages;
    private String brandId;
    private String passwordChangeEndpoint;
    private String unlinkFranceConnectEndpoint;
    private String linkFranceConnectEndpoint;
    private boolean franceConnectEnabled;

    public UIUserProfile() { }

    public UIUserProfile(UserProfile userProfile, List<String> languages, String brandId, String passwordChangeEndpoint,
                         String linkFranceConnectEndpoint, String unlinkFranceConnectEndpoint, boolean franceConnectEnabled) {
        this.userProfile = userProfile;
        this.languages = languages;
        this.brandId = brandId;
        this.passwordChangeEndpoint = passwordChangeEndpoint;
        this.unlinkFranceConnectEndpoint = unlinkFranceConnectEndpoint;
        this.linkFranceConnectEndpoint = linkFranceConnectEndpoint;
        this.franceConnectEnabled = franceConnectEnabled;
    }

    public UIUserProfile(UserProfile userProfile, UserProfile franceConnectProfile, List<String> languages) {
        this.userProfile = userProfile;
        this.franceConnectProfile = franceConnectProfile;
        this.languages = languages;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public UserProfile getFranceConnectProfile() {
        return franceConnectProfile;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public String getPasswordChangeEndpoint() {
        return passwordChangeEndpoint;
    }

    public String getUnlinkFranceConnectEndpoint() {
        return unlinkFranceConnectEndpoint;
    }

    public String getLinkFranceConnectEndpoint() {
        return linkFranceConnectEndpoint;
    }

    public boolean isFranceConnectEnabled() {
        return franceConnectEnabled;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }
}
