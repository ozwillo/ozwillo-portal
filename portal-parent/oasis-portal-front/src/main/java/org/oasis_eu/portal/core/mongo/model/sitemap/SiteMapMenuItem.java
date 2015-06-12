package org.oasis_eu.portal.core.mongo.model.sitemap;

import java.io.Serializable;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * User: lucaterori
 * Date: 08/06/2015
 */
public class SiteMapMenuItem implements Serializable {
    private static final long serialVersionUID = 5598242680668173525L;

    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private String type= "";

    @JacksonXmlProperty(localName = "img_src", isAttribute = true)
    private String img_url= "";

    @JacksonXmlProperty(localName = "href", isAttribute = true)
    private String url = "";

    @JacksonXmlText
    private String label = "";


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

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

    @Override
    public String toString() {

        return "SiteMapMenuItem: { type: " + type
                + ", label : " +label
                + ", img_url : " + img_url
                + ", url : " + url
                + "}";
    }

}
