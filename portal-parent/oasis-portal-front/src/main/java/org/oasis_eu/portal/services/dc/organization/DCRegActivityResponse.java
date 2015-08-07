package org.oasis_eu.portal.services.dc.organization;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DCRegActivityResponse{
    @JsonProperty("areas") private List<DCRegActivity> areas;

    public DCRegActivityResponse(List<DCRegActivity> areas) {this.areas = areas;}

    public List<DCRegActivity> getAreas() {return areas;}

}