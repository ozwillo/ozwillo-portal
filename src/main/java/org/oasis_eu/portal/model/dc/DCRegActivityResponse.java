package org.oasis_eu.portal.model.dc;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DCRegActivityResponse {
    @JsonProperty("areas")
    private List<DCRegActivity> areas;

    public DCRegActivityResponse(List<DCRegActivity> areas) {
        this.areas = areas;
    }

    public List<DCRegActivity> getAreas() {
        return areas;
    }

}
