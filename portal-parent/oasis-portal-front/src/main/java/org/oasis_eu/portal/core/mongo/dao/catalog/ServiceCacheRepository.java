package org.oasis_eu.portal.core.mongo.dao.catalog;

import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 11/21/14
 */
public interface ServiceCacheRepository extends MongoRepository<CatalogEntry, String> {


}
