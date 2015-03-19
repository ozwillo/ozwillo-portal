package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;

import java.util.List;

/**
 * User: schambon
 * Date: 6/13/14
 */
public interface SubscriptionStore {

    void create(String userId, Subscription object);


    /**
     * Used by Dashboard, but NOT pushToDashboard because Kernel returns 403 Forbidden
     * for an other user than oneself (even if admin)
     * @param userId
     * @return
     */
    public List<Subscription> findByUserId(String userId);

    /**
     * Used by push to dashboard by an (for now orga) admin
     * @param serviceId
     * @return
     */
    public List<Subscription> findByServiceId(String serviceId);

    public void unsubscribe(String userId, String serviceId, SubscriptionType subscriptionType);

    public void unsubscribe(String subscriptionId);
}
