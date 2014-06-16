package org.oasis_eu.portal.core.model.appstore;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon
 * Date: 5/14/14
 */
public class LocalService extends AbstractApplication {

    @JsonProperty("application_id")
    private String applicationId;

    @JsonProperty("territory_id")
    private String territoryId;

    @JsonProperty("subscription_url")
    private String subscriptionUrl;


    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getTerritoryId() {
        return territoryId;
    }

    public void setTerritoryId(String territoryId) {
        this.territoryId = territoryId;
    }

    public String getSubscriptionUrl() {
        return subscriptionUrl;
    }

    public void setSubscriptionUrl(String subscriptionUrl) {
        this.subscriptionUrl = subscriptionUrl;
    }
}
