package org.oasis_eu.portal.core.services.sitemap.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMap;

import java.util.List;

/**
 * User: schambon
 * Date: 1/6/15
 */
@JacksonXmlRootElement(localName = "menuset")
public class Footer {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "menu")
    private List<SiteMap> menuset;

    public List<SiteMap> getMenuset() {
        return menuset;
    }

    public void setMenuset(List<SiteMap> menuset) {
        this.menuset = menuset;
    }
}
