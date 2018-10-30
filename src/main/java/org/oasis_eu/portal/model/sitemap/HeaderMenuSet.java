package org.oasis_eu.portal.model.sitemap;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * User: lucaterori
 * Date: 08/06/2015
 */
@JacksonXmlRootElement(localName = "menuset")
public class HeaderMenuSet {

    /**
     * Set in a list one menuset per language (as defined in the xml)
     * menuset [menu, menu, ..., menu ]
     */
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "menu")
    private List<SiteMapMenuHeader> menuset;

    public List<SiteMapMenuHeader> getMenuset() {
        return menuset;
    }

    public void setMenuset(List<SiteMapMenuHeader> menuset) {
        this.menuset = menuset;
    }
}
