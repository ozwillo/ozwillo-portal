package org.oasis_eu.portal.services.geoarea;

import com.mongodb.*;
import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.portal.core.mongo.dao.geo.FTSLanguageMapper;
import org.oasis_eu.portal.core.mongo.dao.geo.GeographicalAreaCache;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalArea;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalAreaReplicationStatus;
import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCOperator;
import org.oasis_eu.spring.datacore.model.DCOrdering;
import org.oasis_eu.spring.datacore.model.DCQueryParameters;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GeographicalAreaService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeographicalAreaService.class);
    
    /** TODO rename prefix ? */
    @Value("${application.geoarea.storageModel:geo:City_0}")
    private String storageModel = "geo:City_0"; // "geo:Area_0"; // "geoci:City_0"
    
    /** TODO LATER rather odisp:name (or field shortcuts) OR RATHER IN CACHE */
    @Value("${application.geoarea.nameField:geo_city:name}")
    private String nameField = "geo_city:name"; // "geoci:name";

    @Value("${application.geoarea.fallbackLanguage:en}")
    private String fallbackLanguage = "en";

    @Value("${application.geoarea.replication_batch_size:100}")
    private int batchSize = 100;

//    @Value("${application.geoarea.replication_query_batch_limit:5}")
//    private int queryBatchLimit = 5; // fetch at most five batches for a query - note that queryBatchLimit / batchSize MUST be lower than the thresholds used in the data core
    
    @Autowired
    private DatacoreClient datacore;

    @Autowired
    private GeographicalAreaCache cache;

    /** to get the current locale */
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private Mongo mongo;

    @Value("${persistence.mongodatabase:portal}")
    private String mongoDb;

    @Autowired
    private MappingMongoConverter mappingMongoConverter;

//    @Scheduled(fixedRate = 24*3600*1000) // every day; TODO: customize that
    @Scheduled(cron = "${application.geoarea.replication}")
    public void replicate() {

        logger.info("Starting replication of geographical data from data core");

        DB db = mongo.getDB(mongoDb);
        DBCollection collection = db.getCollection("geographical_area");
//        collection.setWriteConcern(WriteConcern.ACKNOWLEDGED); // we specifically lower the wc in this case (goes from ~80ms -> ~15ms for each batch insertion)

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
            cache.deleteByStatus(GeographicalAreaReplicationStatus.ONLINE);
            logger.debug("Deleted online data in {} ms", System.currentTimeMillis() - deleteStart);

            // 3. switch all "incoming" entries to "online"
            long switchStart = System.currentTimeMillis();
            cache.switchToOnline();
            logger.debug("Switch to online in {} ms", System.currentTimeMillis() - switchStart);

        } catch(RestClientException e) {
            logger.error("Error while updating the geo area cache", e);

            // "rollback"
            cache.deleteByStatus(GeographicalAreaReplicationStatus.INCOMING);
        }

    }

    private String fetchBatches(DBCollection collection, Set<String> loadedUris, String lastNameFetched) {


        BulkWriteOperation builder = collection.initializeUnorderedBulkOperation();

        DCQueryParameters params =
                lastNameFetched == null ?
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

    /**
     * TODO implement on top of locally cached / sync'd keywords to id map
     * rather than directly on DC 
     * @param q
     * @param start
     * @param limit ex. 11 then return 10 and loadMore=true
     * @return
     */
    public List<GeographicalArea> find(String q, int start, int limit) {

        return cache.search(RequestContextUtils.getLocale(request).getLanguage(), q, start, limit)
                .collect(Collectors.toList());

//
//        q = q.substring(0, 1).toUpperCase() + q.substring(1); // TODO better HACK (using keywords) barcelona => Barcelona
//
//        List<DCResource> res = datacore.findResources(storageModel,
//                new DCQueryParameters(nameField, DCOperator.REGEX, q + ".*"),
//                // , use current i18n
//                start, limit);
//        String preferredLanguage = RequestContextUtils.getLocale(request).getLanguage();
//        return res.stream().map(r -> toGeographicalArea(r, preferredLanguage)).filter(a -> a != null).collect(Collectors.toList());
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
            String l = nameMap.get("l");
            if (l == null) {
                continue; // shouldn't happen
            }
            if (l.equals(language)) {
                name = nameMap.get("v");
                break; // can't find better
            }
            if (name == null || l.equals(fallbackLanguage)) { // only the first time or if fallback
                name = nameMap.get("v");
            }
        }
        area.setName(name);
        area.setUri(r.getUri());
        area.setLang(language);
        area.setFtsLanguage(FTSLanguageMapper.computeMongoLanguage(language));
        //area.setDetailedName(); // TODO fill in Datacore OR RATHER CACHE using names of NUTS3 or else 2 and country
        return area;
    }
            
}
