package org.oasis_eu.portal.front.store;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalArea;

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
