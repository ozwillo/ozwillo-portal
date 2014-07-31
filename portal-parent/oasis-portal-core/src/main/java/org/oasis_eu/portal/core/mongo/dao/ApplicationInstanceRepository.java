package org.oasis_eu.portal.core.mongo.dao;

import org.oasis_eu.portal.core.mongo.model.temp.ApplicationInstanceRegistration;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * User: schambon
 * Date: 7/29/14
 */
public interface ApplicationInstanceRepository extends MongoRepository<ApplicationInstanceRegistration, String> {

    List<ApplicationInstanceRegistration> findByUserId(String userId);

    List<ApplicationInstanceRegistration> findByOrganizationId(String organizationId);
}
