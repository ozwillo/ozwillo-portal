package org.oasis_eu.portal.core.mongo.dao.geo;

import com.mongodb.*;
import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalArea;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalAreaReplicationStatus;
import org.oasis_eu.portal.core.services.search.Tokenizer;
import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCOperator;
import org.oasis_eu.spring.datacore.model.DCOrdering;
import org.oasis_eu.spring.datacore.model.DCQueryParameters;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * User: schambon
 * Date: 4/16/15
 */
@Repository
public class GeographicalAreaCache {

    private static final Logger logger = LoggerFactory.getLogger(GeographicalArea.class);

    @Autowired
    private DatacoreClient datacore;

    @Autowired
    private MongoTemplate template;

    @Autowired
    private Mongo mongo;

    @Autowired
    private MappingMongoConverter mappingMongoConverter;

    @Value("${persistence.mongodatabase:portal}")
    private String mongoDatabaseName;

    @Value("${application.geoarea.replication_batch_size:100}")
    private int batchSize = 100;


    /**
     * TODO rename prefix ?
     */
    @Value("${application.geoarea.storageModel:geo:City_0}")
    private String storageModel = "geo:City_0"; // "geo:Area_0"; // "geoci:City_0"

    /**
     * TODO LATER rather odisp:name (or field shortcuts) OR RATHER IN CACHE
     */
    @Value("${application.geoarea.nameField:geo_city:name}")
    private String nameField = "geo_city:name"; // "geoci:name";

    @Autowired
    private Tokenizer tokenizer;

    public Stream<GeographicalArea> search(String lang, String name, int start, int limit) {

        // This method isn't the nicest to read ever, so here's what it does:
        // 1. tokenize the input and search the cache for each token in the input
        // 2. flatten the results into one big stream
        // 3. reduce the stream into a list of pairs <Area, Frequency>
        // 4. sort the stream in reverse frequency order so that results that match multiple query terms come first
        // 4. turn this back into a stream of areas and return

        class Pair {
            GeographicalArea area;
            int queryTerms;
            int queryMatches = 1;

            public Pair(GeographicalArea area, int queryTerms) {
                this.area = area;
                this.queryTerms = queryTerms;
            }

            public void inc() {
                queryMatches = queryMatches + 1;
            }

            public void inc(int i) {
                queryMatches = queryMatches + i;
            }

            public float score() {
                float l = (float) area.getNameTokens().stream().filter(t -> t.length() >= 3).count();
                float v = queryMatches / Math.max((float) queryTerms, l);
                return v;
            }
        }

        List<String> queryTerms = tokenizer.tokenize(name).stream().filter(t -> t.length() >= 3).collect(Collectors.toList());

        LinkedHashMap<String, Pair> collected = queryTerms
                .stream()
                .flatMap(tok -> findOneToken(lang, tok)) // note that findOneToken doesn't allow duplicate URIs in results
                .collect(LinkedHashMap<String, Pair>::new,
                        (set, area) -> {
                            if (set.containsKey(area.getUri())) {
                                set.get(area.getUri()).inc();
                            } else {
                                set.put(area.getUri(), new Pair(area, queryTerms.size()));
                            }
                        },
                        (set1, set2) -> {
                            for (Pair val : set2.values()) {
                                if (set1.containsKey(val.area.getUri())) {
                                    set1.get(val.area.getUri()).inc(val.queryMatches);
                                } else {
                                    set1.put(val.area.getUri(), val);
                                }
                            }
                        }
                );

        return collected.values()
                .stream()
                .sorted((pair1, pair2) -> new Float(pair2.score()).compareTo(new Float(pair1.score())))
                .map(pair -> pair.area)
                .skip(start)
                .limit(limit);


    }

    private Stream<GeographicalArea> findOneToken(String lang, String name) {
        // we search irrespective of the replication status, but we deduplicate based on DC Resource URI.
        // sort spec means we want older results first - so that incoming replicates are discarded as long as
        // there is an online entry
        return template.find(
                query(where("lang").is(lang).and("nameTokens").regex("^" + name)).with(new Sort(Sort.Direction.ASC, "replicationTime")),
                GeographicalArea.class)
                .stream()
                .map(DCUrlWrapper::new)
                .distinct()
                .map(DCUrlWrapper::unwrap);
    }

    public void save(GeographicalArea area) {
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

    @Scheduled(cron = "${application.geoarea.replication}")
    public void replicate() {

        logger.info("Starting replication of geographical data from data core");

        DB db = mongo.getDB(mongoDatabaseName);
        DBCollection collection = db.getCollection("geographical_area");

        Set<String> loadedUris = new HashSet<>();

        // 1. fetch all the resources from the data core and insert them with status "incoming" in the cache
        try {
            String lastNameFetched = null;

            do {
                logger.debug("Fetching batches of areas");
                lastNameFetched = fetchBatches(collection, loadedUris, lastNameFetched);
            } while (lastNameFetched != null);


            // 2. delete all the "online" entries (they are replaced with the "incoming" ones)
            long deleteStart = System.currentTimeMillis();
            deleteByStatus(GeographicalAreaReplicationStatus.ONLINE);
            logger.debug("Deleted online data in {} ms", System.currentTimeMillis() - deleteStart);

            // 3. switch all "incoming" entries to "online"
            long switchStart = System.currentTimeMillis();
            switchToOnline();
            logger.debug("Switch to online in {} ms", System.currentTimeMillis() - switchStart);

        } catch (RestClientException e) {
            logger.error("Error while updating the geo area cache", e);

            // "rollback"
            deleteByStatus(GeographicalAreaReplicationStatus.INCOMING);
        }

    }

    private String fetchBatches(DBCollection collection, Set<String> loadedUris, String lastNameFetched) {


        BulkWriteOperation builder = collection.initializeUnorderedBulkOperation();

        DCQueryParameters params;
        params = lastNameFetched == null ?
                new DCQueryParameters(nameField, DCOrdering.ASCENDING) :
                new DCQueryParameters(nameField, DCOrdering.ASCENDING, DCOperator.GT, lastNameFetched);

        logger.debug("Querying the Data Core");
        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(storageModel, params, 0, batchSize);
        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd - queryStart);

        boolean hasOne = false;

        for (DCResource res : resources) {
            String name = null;

            for (Languages language : Languages.values()) {

                GeographicalArea area = toGeographicalArea(res, language.getLanguage());
                if (area == null) {
                    continue;
                }

                if (!loadedUris.contains(language.getLanguage() + area.getUri())) {
                    hasOne = true;
                    if (name == null) {
                        name = area.getName();
                        logger.debug("{} - {}", name, area.getUri());
                    }

                    DBObject dbObject = new BasicDBObject();
                    mappingMongoConverter.write(area, dbObject);

                    builder.insert(dbObject);

                    loadedUris.add(language.getLanguage() + area.getUri());
                } else {
                    if (name == null) {
                        name = area.getName();
                        logger.debug("Area {} already inserted for language {}, skipping", area.getName(), language.getLanguage());
                    }
                }

            }

            if (name != null) lastNameFetched = name;

        }

        if (hasOne) {
            long st = System.currentTimeMillis();
            builder.execute();
            long durationSave = System.currentTimeMillis() - st;
            logger.debug("Saved resources; total save time={} ms (avg = {} ms)", durationSave, durationSave / resources.size());
        }


        if (resources.size() < batchSize) return null;
        else return lastNameFetched;
    }

    private GeographicalArea toGeographicalArea(DCResource r, String language) {
        GeographicalArea area = new GeographicalArea();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> nameMaps = (List<Map<String, String>>) r.get(nameField);
        if (nameMaps == null) {
            logger.warn("DC Resource {} of type {} has no field named {}", r.getUri(), r.getType(), nameField);
            return null;
        }
        String name = null;
        for (Map<String, String> nameMap : nameMaps) {
            String l = nameMap.get("@language");
            if (l == null) {
                continue; // shouldn't happen
            }
            if (l.equals(language)) {
                name = nameMap.get("@value");
                break; // can't find better
            }
            if (name == null) {
                name = nameMap.get("@value");
            }
        }
        area.setName(name);
        area.setUri(r.getUri());
        area.setLang(language);
        area.setNameTokens(tokenizer.tokenize(name));
        //area.setDetailedName(); // TODO fill in Datacore OR RATHER CACHE using names of NUTS3 or else 2 and country
        return area;
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