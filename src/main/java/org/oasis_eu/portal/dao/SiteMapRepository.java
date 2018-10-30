package org.oasis_eu.portal.dao;

import org.oasis_eu.portal.model.sitemap.SiteMapMenuFooter;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 12/15/14
 */
public interface SiteMapRepository extends MongoRepository<SiteMapMenuFooter, String> {

    SiteMapMenuFooter findByLanguage(String language);
}
