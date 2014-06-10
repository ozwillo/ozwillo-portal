package org.oasis_eu.portal.core.model;

import java.util.Locale;

/**
 * User: schambon
 * Date: 5/14/14
 */
public class SearchControls {
    private Audience[] audience;
    private AppstoreCategory[] categories;
    private Locale locale;

    public SearchControls(Locale locale) {
        this.locale = locale;
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

    public void setCategories(AppstoreCategory[] categories) {
        this.categories = categories;
    }
}
