package org.oasis_eu.portal.dao;

import org.oasis_eu.portal.model.sitemap.SiteMapComponents;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SiteMapComponentsRepository extends MongoRepository<SiteMapComponents, String> {

    SiteMapComponents findByWebsite(String website);


}
