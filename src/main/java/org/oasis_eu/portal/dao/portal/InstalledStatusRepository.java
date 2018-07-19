package org.oasis_eu.portal.dao.portal;

import org.oasis_eu.portal.model.kernel.store.CatalogEntryType;
import org.oasis_eu.portal.model.store.InstalledStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 11/5/14
 */
public interface InstalledStatusRepository extends MongoRepository<InstalledStatus, String> {

    InstalledStatus findByCatalogEntryTypeAndCatalogEntryIdAndUserId(CatalogEntryType catalogEntryType, String catalogEntryId, String userId);

}
