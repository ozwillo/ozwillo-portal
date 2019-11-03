package org.oasis_eu.portal.model.sitemap;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class SiteMapEntry implements Serializable {
    private static final long serialVersionUID = 5598242680668173525L;

    @JsonProperty(value = "href")
    private String url;

    private Integer row;

    private String label;

    public SiteMapEntry() {
    }

    public SiteMapEntry(String url, Integer row, String label) {
        this.url = url;
        this.row = row;
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
