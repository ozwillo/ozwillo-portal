package org.oasis_eu.portal.core.mongo.dao.cms;

import org.oasis_eu.portal.core.mongo.model.cms.ContentItem;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 6/11/14
 */
public interface ContentItemRepository extends MongoRepository<ContentItem, String> {

}
