package org.oasis_eu.portal.core.model.subscription;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Instant;
import org.oasis_eu.portal.core.model.appstore.GenericEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: schambon
 * Date: 6/12/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Subscription extends GenericEntity {

    private static final Logger logger = LoggerFactory.getLogger(Subscription.class);

    @JsonProperty("id")
    private String id;

    @JsonProperty("user_id")
    private String userId;

    /**
     * Application or local service id
     */
    @JsonProperty("service_id")
    private String serviceId;

    /**
     * Is this subscription made in a professional or personal context?
     */
    @JsonProperty("subscription_type")
    private SubscriptionType subscriptionType;

    @JsonProperty("subscription_uri")
    private String subscriptionUri;

    @JsonProperty("creator_id")
    private String creatorId;

    private Instant modified;

    @JsonAnySetter
    private void anySetter(String key, String value) {
        logger.warn("Unknown property {} fetched from JSON: {}", key, value);
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(SubscriptionType subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public Instant getModified() {
        return modified;
    }

    public void setModified(Instant modified) {
        this.modified = modified;
    }

    public String getSubscriptionUri() {
        return subscriptionUri;
    }

    public void setSubscriptionUri(String subscriptionUri) {
        this.subscriptionUri = subscriptionUri;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", subscriptionType=" + subscriptionType +
                ", subscriptionUri='" + subscriptionUri + '\'' +
                ", creatorId='" + creatorId + '\'' +
                ", modified=" + modified +
                '}';
    }
}
