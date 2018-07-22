package org.oasis_eu.portal.dao;

import org.oasis_eu.portal.model.dashboard.Dashboard;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 6/17/14
 */
public interface DashboardRepository extends MongoRepository<Dashboard, String> {
}
