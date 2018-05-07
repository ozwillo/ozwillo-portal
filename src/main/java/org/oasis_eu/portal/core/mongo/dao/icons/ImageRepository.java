package org.oasis_eu.portal.core.mongo.dao.icons;

import org.joda.time.DateTime;
import org.oasis_eu.portal.core.mongo.model.images.Image;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * User: schambon
 * Date: 8/21/14
 */
public interface ImageRepository extends MongoRepository<Image, String> {

    Image findByUrl(String url);

    List<Image> findByDownloadedTimeBefore(DateTime before, Pageable page);
}
