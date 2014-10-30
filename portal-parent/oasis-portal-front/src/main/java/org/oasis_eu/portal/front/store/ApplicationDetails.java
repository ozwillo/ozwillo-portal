package org.oasis_eu.portal.front.store;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * User: schambon
 * Date: 10/29/14
 */
public class ApplicationDetails {
    @JsonProperty
    int rating;
    @JsonProperty
    String policy;
    @JsonProperty
    String tos;
    @JsonProperty
    String longdescription;
    @JsonProperty
    List<String> screenshots;

}
