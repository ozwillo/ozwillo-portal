package org.oasis_eu.portal.front.store;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * User: schambon
 * Date: 12/16/14
 */
public class StoreAppResponse {

	@JsonProperty("apps")
	private List<StoreApplication> apps;
	@JsonProperty("maybeMoreApps")
	private boolean maybeMoreApps;


	public StoreAppResponse(List<StoreApplication> apps, boolean maybeMoreApps) {
		this.apps = apps;
		this.maybeMoreApps = maybeMoreApps;
	}

	public List<StoreApplication> getApps() {
		return apps;
	}

	public boolean isMaybeMoreApps() {
		return maybeMoreApps;
	}

}
