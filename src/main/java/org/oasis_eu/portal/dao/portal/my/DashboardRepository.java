package org.oasis_eu.portal.dao.portal.my;

import org.oasis_eu.portal.model.my.Dashboard;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 6/17/14
 */
public interface DashboardRepository extends MongoRepository<Dashboard, String> {
}
