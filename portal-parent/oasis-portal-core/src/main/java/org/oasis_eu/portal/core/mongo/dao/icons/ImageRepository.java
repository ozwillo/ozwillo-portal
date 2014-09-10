package org.oasis_eu.portal.core.mongo.dao.icons;

import org.oasis_eu.portal.core.mongo.model.images.Image;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * User: schambon
 * Date: 8/21/14
 */
public interface ImageRepository extends MongoRepository<Image, String> {

    public Image findByUrl(String url);
}
