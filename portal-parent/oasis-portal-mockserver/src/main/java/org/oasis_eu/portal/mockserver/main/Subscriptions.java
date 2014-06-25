package org.oasis_eu.portal.mockserver.main;

import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 6/24/14
 */
public interface Subscriptions extends MongoRepository<Subscription, String> {
}
