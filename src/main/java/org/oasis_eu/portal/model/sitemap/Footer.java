package org.oasis_eu.portal.model.sitemap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;
import java.util.List;

/**
 * User: schambon
 * Date: 1/6/15
 */
public class Footer implements Serializable {
    @JsonProperty(value = "menu")
    private List<SiteMapMenuFooter> menuset;

    public List<SiteMapMenuFooter> getMenuset() {
        return menuset;
    }

    public void setMenuset(List<SiteMapMenuFooter> menuset) {
        this.menuset = menuset;
    }
}
