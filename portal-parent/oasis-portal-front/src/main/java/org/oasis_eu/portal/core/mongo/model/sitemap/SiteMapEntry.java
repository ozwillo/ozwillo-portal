package org.oasis_eu.portal.core.mongo.model.sitemap;

import java.io.Serializable;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * User: schambon
 * Date: 12/15/14
 */
public class SiteMapEntry implements Serializable {
    private static final long serialVersionUID = 5598242680668173525L;

    @JacksonXmlProperty(localName = "href", isAttribute = true)
    private String url = "";
    @JacksonXmlText
    private String label = "";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
