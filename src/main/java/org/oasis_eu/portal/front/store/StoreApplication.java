package org.oasis_eu.portal.front.store;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon
 * Date: 10/29/14
 */
public class StoreApplication {

    @JsonProperty
    String id;
    @JsonProperty
    String name;
    @JsonProperty
    Type type;
    @JsonProperty
    String icon;
    @JsonProperty("public_service")
    boolean publicService;
    @JsonProperty
    String description;
    @JsonProperty("provider")
    String providerName;
    @JsonProperty
    boolean paid;
    @JsonProperty("target_citizens")
    boolean audienceCitizens;
    @JsonProperty("target_publicbodies")
    boolean audiencePublicBodies;
    @JsonProperty("target_companies")
    boolean audienceCompanies;

    @JsonProperty
    boolean installed;

    public enum Type {
        service,
        application
    }

}
