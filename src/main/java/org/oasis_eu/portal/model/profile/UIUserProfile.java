package org.oasis_eu.portal.model.profile;

import org.oasis_eu.portal.model.kernel.UserProfile;

import java.util.List;

public class UIUserProfile {

    private UserProfile userProfile;
    private UserProfile franceConnectProfile;
    private List<String> languages;
    private String passwordChangeEndpoint;

    public UIUserProfile() { }

    public UIUserProfile(UserProfile userProfile, List<String> languages, String passwordChangeEndpoint) {
        this.userProfile = userProfile;
        this.languages = languages;
        this.passwordChangeEndpoint = passwordChangeEndpoint;
    }

    public UIUserProfile(UserProfile userProfile, UserProfile franceConnectProfile, List<String> languages, String passwordChangeEndpoint) {
        this.userProfile = userProfile;
        this.franceConnectProfile = franceConnectProfile;
        this.languages = languages;
        this.passwordChangeEndpoint = passwordChangeEndpoint;
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
}
