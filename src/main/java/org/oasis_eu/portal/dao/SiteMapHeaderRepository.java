package org.oasis_eu.portal.dao;

import org.oasis_eu.portal.model.sitemap.SiteMapMenuSet;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 12/15/14
 */
public interface SiteMapHeaderRepository extends MongoRepository<SiteMapMenuSet, String> {

    SiteMapMenuSet findByLanguage(String language);
}
