package org.oasis_eu.portal.core.model.appstore;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used to communicate with the kernel in the app instantiation process
 * User: schambon
 * Date: 7/24/14
 */
public class AppInstance {

    @JsonProperty("provider_id")
    private String providerId;

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
