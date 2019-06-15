package org.oasis_eu.portal.dao;

import org.oasis_eu.portal.model.sitemap.SiteMapComponents;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteMapComponentsRepository extends MongoRepository<SiteMapComponents, String> {

    Optional<SiteMapComponents> findByWebsite(String website);
}
