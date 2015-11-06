package org.oasis_eu.portal.core.mongo.dao.my;

import org.oasis_eu.portal.core.mongo.model.my.HiddenPendingApps;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 4/16/15
 */
public interface HiddenPendingAppsRepository extends MongoRepository<HiddenPendingApps, String> {
}
