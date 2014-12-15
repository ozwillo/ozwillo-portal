package org.oasis_eu.portal.core.mongo.model.sitemap;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * User: schambon
 * Date: 12/15/14
 */
@Document(collection = "sitemap")
public class SiteMap {

    @Id
    private String id;

    @Indexed(unique = true)
    private String language;

    private List<SiteMapEntry> entries = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<SiteMapEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<SiteMapEntry> entries) {
        this.entries = entries;
    }
}
