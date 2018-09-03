package org.oasis_eu.portal.model.kernel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used to communicate with the kernel in the app instantiation process
 * User: schambon
 * Date: 7/24/14
 */
public class ApplicationInstantiationRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("provider_id")
    private String providerId;

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        return "ApplicationInstantiationRequest{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", providerId='" + providerId + '\'' +
            '}';
    }
}
