package org.oasis_eu.portal.core.model.appstore;

import java.util.Locale;

/**
 * User: schambon
 * Date: 5/14/14
 */
public class SearchControls {
    private Audience[] audience;
    private AppstoreCategory[] categories;
    private String language;

    public SearchControls(String language) {
        this.language = language;
    }

    public void setAudience(Audience... audience) {
        this.audience = audience;
    }

    public Audience[] getAudience() {
        return audience;
    }

    public AppstoreCategory[] getCategories() {
        return categories;
    }

    public void setCategories(AppstoreCategory... categories) {
        this.categories = categories;
    }

    public String getLanguage() {
        return language;
    }
}
