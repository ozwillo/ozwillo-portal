package org.oasis_eu.portal.dao.portal.my;

import org.oasis_eu.portal.model.my.HiddenPendingApps;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 4/16/15
 */
public interface HiddenPendingAppsRepository extends MongoRepository<HiddenPendingApps, String> {
}
