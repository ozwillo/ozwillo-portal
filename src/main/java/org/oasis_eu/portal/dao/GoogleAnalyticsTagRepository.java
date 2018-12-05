package org.oasis_eu.portal.dao;

import org.oasis_eu.portal.model.sitemap.GoogleAnalyticsTag;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GoogleAnalyticsTagRepository extends MongoRepository<GoogleAnalyticsTag, String> {
    GoogleAnalyticsTag findByWebsite(String website);
}
