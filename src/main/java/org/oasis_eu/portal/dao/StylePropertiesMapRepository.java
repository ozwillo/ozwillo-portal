package org.oasis_eu.portal.dao;

import org.oasis_eu.portal.model.sitemap.StylePropertiesMap;
import org.oasis_eu.portal.model.sitemap.StyleProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StylePropertiesMapRepository extends MongoRepository<StylePropertiesMap, String> {

    StylePropertiesMap findByWebsite(String website);
}
