package org.oasis_eu.portal.core.mongo.dao.store;

import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.core.mongo.model.store.InstalledStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 11/5/14
 */
public interface InstalledStatusRepository extends MongoRepository<InstalledStatus, String> {

    InstalledStatus findByCatalogEntryTypeAndCatalogEntryIdAndUserId(CatalogEntryType catalogEntryType, String catalogEntryId, String userId);

}
