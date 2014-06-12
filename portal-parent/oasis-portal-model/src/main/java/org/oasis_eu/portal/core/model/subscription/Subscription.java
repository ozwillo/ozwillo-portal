package org.oasis_eu.portal.core.model.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Instant;

/**
 * User: schambon
 * Date: 6/12/14
 */
public class Subscription {

    private String id;

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

    /**
     * Identifier of the user context for this subscription
     */
    @JsonProperty("user_context_id")
    private String userContextId;

    private Instant created;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getUserContextId() {
        return userContextId;
    }

    public void setUserContextId(String userContextId) {
        this.userContextId = userContextId;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }
}
