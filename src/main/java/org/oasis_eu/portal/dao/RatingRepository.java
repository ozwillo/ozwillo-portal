package org.oasis_eu.portal.dao;

import org.oasis_eu.portal.model.kernel.store.CatalogEntryType;
import org.oasis_eu.portal.model.store.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * User: schambon
 * Date: 10/31/14
 */
public interface RatingRepository extends MongoRepository<Rating, String> {

    List<Rating> findByAppTypeAndAppIdAndUserId(CatalogEntryType appType, String appId, String userId);

}
