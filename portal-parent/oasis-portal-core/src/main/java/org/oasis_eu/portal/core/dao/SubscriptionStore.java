package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.subscription.Subscription;

import java.util.List;

/**
 * User: schambon
 * Date: 6/13/14
 */
public interface SubscriptionStore extends GenericCRUDStore<Subscription> {

    public List<Subscription> findByUserId(String userId);

}
