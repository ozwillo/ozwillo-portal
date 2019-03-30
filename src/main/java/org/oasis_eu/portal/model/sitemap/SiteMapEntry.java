package org.oasis_eu.portal.model.sitemap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import java.io.Serializable;

/**
 * User: schambon
 * Date: 12/15/14
 */
public class SiteMapEntry implements Serializable {
    private static final long serialVersionUID = 5598242680668173525L;

    @JsonProperty(value = "href")
    private String url = "";

    private Integer row = 0;

    private String label = "";

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
