package org.oasis_eu.portal.core.mongo.dao.icons;

import org.oasis_eu.portal.core.mongo.model.icons.IconDownloadAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 9/3/14
 */
public interface IconDownloadAttemptRepository extends MongoRepository<IconDownloadAttempt, String> {

    IconDownloadAttempt findByUrl(String url);

}
