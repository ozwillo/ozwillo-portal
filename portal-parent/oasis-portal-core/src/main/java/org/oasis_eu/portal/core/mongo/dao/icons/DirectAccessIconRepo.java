package org.oasis_eu.portal.core.mongo.dao.icons;

import com.mongodb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Use MongoDB's Java driver directly to get the hash of the icon.
 * The idea is that I don't necessarily want to burden the network with passing
 * icons' payload to and fro when I only want to check if they've been changed.
 *
 * User: schambon
 * Date: 9/2/14
 */
@Component
public class DirectAccessIconRepo {
    @Autowired
    private Mongo mongo;

    @Value("${persistence.mongodatabase}")
    private String databaseName;

    public String getHashForIcon(String id) {

        DBCollection icons = mongo.getDB(databaseName).getCollection("icon");

        DBObject query = new BasicDBObject("_id", id);
        DBObject projection = new BasicDBObject("hash", 1);

        DBObject hashDoc = icons.findOne(query, projection);

        return hashDoc != null ? (String) hashDoc.get("hash") : null;

    }

}

