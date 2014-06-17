package org.oasis_eu.portal.core.model.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Instant;
import org.oasis_eu.portal.core.model.appstore.GenericEntity;

/**
 * User: schambon
 * Date: 6/12/14
 */
public class Subscription extends GenericEntity {


    @JsonProperty("user_id")
    private String userId;

    /**
     * Is this an application or a local service?
     */
    @JsonProperty("application_type")
    private ApplicationType applicationType;

    /**
     * Application or local service id
     */
    @JsonProperty("application_id")
    private String applicationId;

    /**
     * Is this subscription made in a professional or personal context?
     */
    @JsonProperty("subscription_type")
    private SubscriptionType subscriptionType;

    private Instant created;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(SubscriptionType subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }
}
