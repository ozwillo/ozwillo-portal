package org.oasis_eu.portal.controller.store;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.model.geo.GeographicalArea;

import java.util.List;

public class GeographicalAreaResponse {

    @JsonProperty("areas")
    private List<GeographicalArea> areas;
    @JsonProperty("maybeMore")
    private boolean maybeMore;


    public GeographicalAreaResponse(List<GeographicalArea> areas, boolean maybeMore) {
        this.areas = areas;
        this.maybeMore = maybeMore;
    }

    public List<GeographicalArea> getAreas() {
        return areas;
    }

    public boolean isMaybeMore() {
        return maybeMore;
    }

}
