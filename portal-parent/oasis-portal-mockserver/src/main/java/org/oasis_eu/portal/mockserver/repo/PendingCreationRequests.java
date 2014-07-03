package org.oasis_eu.portal.mockserver.repo;

import org.oasis_eu.portal.mockserver.appstore.CreateInstanceRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 7/3/14
 */
public interface PendingCreationRequests extends MongoRepository<CreateInstanceRequest, String> {
}
