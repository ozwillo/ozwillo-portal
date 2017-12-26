package org.oasis_eu.portal.core.mongo.dao.store;

import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.core.mongo.model.store.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * User: schambon
 * Date: 10/31/14
 */
public interface RatingRepository extends MongoRepository<Rating, String> {

    List<Rating> findByAppTypeAndAppIdAndUserId(CatalogEntryType appType, String appId, String userId);

}
