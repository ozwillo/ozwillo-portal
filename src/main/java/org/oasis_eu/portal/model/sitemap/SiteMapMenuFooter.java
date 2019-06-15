package org.oasis_eu.portal.model.sitemap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 12/15/14
 */
public class SiteMapMenuFooter implements Serializable {

    @Id
    @JsonIgnore
    private String id;

    @JsonProperty(value = "locale")
    private String language;

    @JsonProperty(value = "item")
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

    public List<SiteMapEntry> getSMEEntries(String webHome) {
        return entries.stream()
                .peek(siteMapEntry -> siteMapEntry.setUrl(webHome + siteMapEntry.getUrl()))
                .collect(Collectors.toList());
    }

    public void setEntries(List<SiteMapEntry> entries) {
        this.entries = entries;
    }
}
