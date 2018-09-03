package org.oasis_eu.portal.dao;

import org.oasis_eu.portal.model.dashboard.HiddenPendingApps;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 4/16/15
 */
public interface HiddenPendingAppsRepository extends MongoRepository<HiddenPendingApps, String> {
}
