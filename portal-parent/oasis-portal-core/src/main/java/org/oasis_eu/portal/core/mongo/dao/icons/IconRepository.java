package org.oasis_eu.portal.core.mongo.dao.icons;

import org.oasis_eu.portal.core.mongo.model.icons.Icon;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 8/21/14
 */
public interface IconRepository extends MongoRepository<Icon, String> {

    public Icon findByUrl(String url);
}
