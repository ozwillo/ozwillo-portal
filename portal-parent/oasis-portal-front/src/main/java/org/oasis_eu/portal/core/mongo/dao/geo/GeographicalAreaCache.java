package org.oasis_eu.portal.core.mongo.dao.geo;

import org.oasis_eu.portal.core.mongo.model.geo.GeographicalArea;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalAreaReplicationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * User: schambon
 * Date: 4/16/15
 */
@Repository
public class GeographicalAreaCache {

    @Autowired
    private MongoTemplate template;

    // we search irrespective of the replication status, but we deduplicate based on DC Resource URI.
    // also we return a Stream since it's likely how we're going to use the data anyhow
    public Stream<GeographicalArea> search(String lang, String name, int start, int limit) {

        TextQuery query = TextQuery.queryText(TextCriteria.forLanguage(FTSLanguageMapper.computeMongoLanguage(lang)).matching(name)).sortByScore();

        return template.find(query, GeographicalArea.class)
                .stream()
                .map(DCUrlWrapper::new)
                .distinct()
                .map(DCUrlWrapper::unwrap)
                .skip(start)
                .limit(limit);

//        return template.find(
//                query(where("lang").is(lang).and("name").regex("^" + name, "i")).with(new Sort(Sort.Direction.DESC, "replicationTime")),
//                GeographicalArea.class)
//                .stream()
//                .map(DCUrlWrapper::new)
//                .distinct()
//                .map(DCUrlWrapper::unwrap)
//                .skip(start)
//                .limit(limit);
    }

    public void save(GeographicalArea area) {
        area.setFtsLanguage(FTSLanguageMapper.computeMongoLanguage(area.getLang()));
        template.save(area);
    }

    public int deleteByStatus(GeographicalAreaReplicationStatus status) {
        return template.remove(query(where("status").is(status)), GeographicalArea.class).getN();
    }

    /**
     * Switch all "Incoming" entries to "Online"
     */
    public void switchToOnline() {
        template.updateMulti(
                query(where("status").is(GeographicalAreaReplicationStatus.INCOMING)),
                Update.update("status", GeographicalAreaReplicationStatus.ONLINE),
                GeographicalArea.class
        );
    }
}

class DCUrlWrapper {
    private final GeographicalArea area;

    public DCUrlWrapper(GeographicalArea area) {
        this.area = area;
    }

    public GeographicalArea unwrap() {
        return area;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DCUrlWrapper) {
            return this.area.getUri().equals(((DCUrlWrapper)other).area.getUri());
        } else return false;
    }

    @Override
    public int hashCode() {
        return area.getUri().hashCode();
    }
}