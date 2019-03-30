package org.oasis_eu.portal.model.sitemap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


@Document(collection = "sitemap_components")
public class SiteMapComponents {
    @Id
    @JsonIgnore
    private String id;

    private String website = "";

    private List<SiteMapMenuFooter> siteMapMenuFooter = new ArrayList<>();

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<SiteMapMenuFooter> getSiteMapMenuFooter() {
        return siteMapMenuFooter;
    }

    public void setSiteMapMenuFooter(List<SiteMapMenuFooter> siteMapMenuFooter) {
        this.siteMapMenuFooter = siteMapMenuFooter;
    }
}
