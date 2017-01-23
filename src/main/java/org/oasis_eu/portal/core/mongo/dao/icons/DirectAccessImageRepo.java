package org.oasis_eu.portal.core.mongo.dao.icons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Use MongoDB's Java driver directly to get the hash of the icon.
 * The idea is that I don't necessarily want to burden the network with passing
 * icons' payload to and fro when I only want to check if they've been changed.
 * <p>
 * User: schambon
 * Date: 9/2/14
 */
@Component
public class DirectAccessImageRepo {

    @Autowired
    private MongoTemplate mongoTemplate;

    public String getHashForIcon(String id) {

        Query query = new Query(where("_id").is(id));
        query.fields().include("hash");
        return mongoTemplate.findOne(query, String.class, "image");
    }
}

