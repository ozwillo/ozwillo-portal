package org.oasis_eu.portal.core.mongo.dao.my;

import org.oasis_eu.portal.core.mongo.model.my.DashboardOrdering;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * User: schambon
 * Date: 6/12/14
 */
public interface DashboardOrderingRepository extends MongoRepository<DashboardOrdering, String> {

    public List<DashboardOrdering> findByUserId(String userId);

}
