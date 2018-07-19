package org.oasis_eu.portal.dao.portal;

import org.oasis_eu.portal.model.sitemap.SiteMap;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 12/15/14
 */
public interface SiteMapRepository extends MongoRepository<SiteMap, String> {

    SiteMap findByLanguage(String language);
}
