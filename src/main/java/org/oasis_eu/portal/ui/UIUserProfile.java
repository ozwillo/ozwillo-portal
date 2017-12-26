package org.oasis_eu.portal.ui;

import org.oasis_eu.portal.model.user.UserProfile;

import java.util.List;

public class UIUserProfile {

    private UserProfile userProfile;
    private UserProfile franceConnectProfile;
    private List<String> languages;
    private String passwordChangeEndpoint;
    private String unlinkFranceConnectEndpoint;
    private String linkFranceConnectEndpoint;

    public UIUserProfile() { }

    public UIUserProfile(UserProfile userProfile, List<String> languages, String passwordChangeEndpoint,
                         String linkFranceConnectEndpoint, String unlinkFranceConnectEndpoint) {
        this.userProfile = userProfile;
        this.languages = languages;
        this.passwordChangeEndpoint = passwordChangeEndpoint;
        this.unlinkFranceConnectEndpoint = unlinkFranceConnectEndpoint;
        this.linkFranceConnectEndpoint = linkFranceConnectEndpoint;
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
}
