package org.oasis_eu.portal.model.sitemap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "google_analytics_tags")
public class GoogleAnalyticsTag {

    @Id
    @JsonIgnore
    private String id;

    private String tag;

    private String website;

    public GoogleAnalyticsTag() {
    }

    public GoogleAnalyticsTag(String tag, String website) {
        this.tag = tag;
        this.website = website;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
