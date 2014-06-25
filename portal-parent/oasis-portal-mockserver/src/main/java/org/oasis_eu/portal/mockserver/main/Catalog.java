package org.oasis_eu.portal.mockserver.main;

import org.oasis_eu.portal.core.model.appstore.Audience;
import org.oasis_eu.portal.core.model.appstore.CatalogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * User: schambon
 * Date: 6/24/14
 */
public interface Catalog extends MongoRepository<CatalogEntry, String> {

    List<CatalogEntry> findByVisible(boolean visible);

    List<CatalogEntry> findByVisibleAndTargetAudienceIn(boolean visible, List<Audience> targetAudience);
}
