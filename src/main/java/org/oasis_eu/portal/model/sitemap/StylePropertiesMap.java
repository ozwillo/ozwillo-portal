package org.oasis_eu.portal.model.sitemap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


@Document(collection = "style_properties")
public class StylePropertiesMap {
    @Id
    @JsonIgnore
    private String id;


    private List<StyleProperty> styleProperties = new ArrayList<>();


    private String website;

    public StylePropertiesMap() {}

    public StylePropertiesMap(List<StyleProperty> styleProperties, String website) {
        this.styleProperties = styleProperties;
        this.website = website;
    }

    public List<StyleProperty> getStyleProperties() {
        return styleProperties;
    }

    public void setStyleProperties(List<StyleProperty> styleProperties) {
        this.styleProperties = styleProperties;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
