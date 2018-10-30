package org.oasis_eu.portal.model.sitemap;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class FooterMenuSet implements Serializable {
    @JsonProperty(value = "menuset")
    private Footer footer;

    public Footer getFooter() {
        return footer;
    }

    public void setFooter(Footer footer) {
        this.footer = footer;
    }
}
