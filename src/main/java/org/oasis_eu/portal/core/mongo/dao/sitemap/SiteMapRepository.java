package org.oasis_eu.portal.core.mongo.dao.sitemap;

import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMap;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 12/15/14
 */
public interface SiteMapRepository extends MongoRepository<SiteMap, String> {

    SiteMap findByLanguage(String language);
}
