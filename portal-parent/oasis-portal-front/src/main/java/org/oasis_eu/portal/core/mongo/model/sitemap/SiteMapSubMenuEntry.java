package org.oasis_eu.portal.core.mongo.model.sitemap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * User: lucaterori
 * Date: 08/06/2015
 */
public class SiteMapSubMenuEntry implements Serializable {
    private static final long serialVersionUID = 1125491325312102355L;

    @JacksonXmlProperty(localName = "href", isAttribute = true)
    private String url = "";

    @JacksonXmlProperty(localName = "title")
    private String title = "";

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    private List<SiteMapMenuItem> subItems = new ArrayList<>();


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<SiteMapMenuItem> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<SiteMapMenuItem> subItems) {
        this.subItems = subItems;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String label) {
        this.title = label;
    }

    @Override
    public String toString() {

        return "SiteMapMenuItem: { "
                + ", title : " + title
                + ", url : " + url
                + ", subItems : "  + subItems.toString()
                + "}";
    }

}
