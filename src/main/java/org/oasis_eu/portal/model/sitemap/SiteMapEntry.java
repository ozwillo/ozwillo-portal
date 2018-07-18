package org.oasis_eu.portal.model.sitemap;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import java.io.Serializable;

/**
 * User: schambon
 * Date: 12/15/14
 */
public class SiteMapEntry implements Serializable {
    private static final long serialVersionUID = 5598242680668173525L;

    @JacksonXmlProperty(localName = "href", isAttribute = true)
    private String url = "";

    @JacksonXmlProperty(localName = "row", isAttribute = true)
    private Integer row = 0;

    @JacksonXmlText
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
