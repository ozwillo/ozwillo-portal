package org.oasis_eu.portal.front.store;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeographicalArea {
    
    /** displayed ; in current locale */
    @JsonProperty
    private String name;
    /** to help the user discriminate, built using names of NUTS3 or else 2 parent with country */
    @JsonProperty
    private String detailedName;
    /** URI in Datacore (required if ex. sending directly to store ajax) */
    @JsonProperty
    private String uri;
    
    public GeographicalArea() {
        
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetailedName() {
        return detailedName;
    }

    public void setDetailedName(String detailedName) {
        this.detailedName = detailedName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
    
}
