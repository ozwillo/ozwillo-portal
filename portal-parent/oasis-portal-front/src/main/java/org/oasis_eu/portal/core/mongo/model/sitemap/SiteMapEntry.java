package org.oasis_eu.portal.core.mongo.model.sitemap;

import java.io.Serializable;

/**
 * User: schambon
 * Date: 12/15/14
 */
public class SiteMapEntry implements Serializable {

    private String url;
    private String label;

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
