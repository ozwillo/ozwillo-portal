package org.oasis_eu.portal.dao;

import org.oasis_eu.portal.model.Languages;
import org.oasis_eu.portal.model.geo.GeographicalArea;
import org.oasis_eu.portal.model.geo.GeographicalAreaReplicationStatus;
import org.oasis_eu.portal.model.search.Tokenizer;
import org.oasis_eu.portal.services.SystemUserService;
import org.oasis_eu.portal.services.dc.GeographicalDAO;
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
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * TODO move generic & org stuff outside, better Service vs DAO archi
 * <p>
 * TODO LATER make generic DatacoreBusinessFulltextCache<BO> (business object)
 * and concretize GeographicalAreaFulltextCache extends DatacoreBusinessFulltextCache<GeographicalArea>
 * created in its own @Configuration class see http://stackoverflow.com/questions/11845871/spring-value-annotation-inheritance-and-common-fields-but-different-values
 * <p>
 * User: schambon
 * Date: 4/16/15
 */
@Repository
public class GeographicalAreaCache {

    private static final Logger logger = LoggerFactory.getLogger(GeographicalArea.class);

    @Autowired
    private DatacoreClient datacore;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GeographicalDAO geographicalDAO;

    @Value("${application.geoarea.replication_batch_size:100}")
    private int batchSize = 100;

    /**
     * geo_0/1... can also be used to use a version not yet published (i.e. made visible in geo)
     */
    @Value("${application.geoarea.project:geo}")
    private String project;

    @Value("${application.geoarea.areaModel:geo:Area_0}")
    private String areaModel; //"geo:Area_0"; // "geoci:City_0"
    /**
     * used as secondary search field in cache ex. Rhône-Alpes
     */
    @Value("${application.geoarea.nameField:geo:name}")
    private String nameField; // or city specific "geoci:name", country-specific "geoco:name"
    /**
     * USED AS PRIMARY SEARCH FIELD IN CACHE ex. Rhône-Alpes, France
     */
    @Value("${application.geoarea.displayNameField:odisp:name}")
    private String displayNameField;// or geo specific "geo:displayName"
    /**
     * ex. "http://data.ozwillo.com/dc/type/geocifr:Commune_0/FR/FR-38/Saint-Clair-de-la-Tour"
     */
    @Value("${application.geoarea.searchCronField:@id}")
    private String searchCronField; // "@id";
    @Value("${application.geoarea.findOneTokenLimit:100}")
    private int findOneTokenLimit;

    @Autowired
    private Tokenizer tokenizer;
    @Autowired
    SystemUserService systemUserService;

    public Stream<GeographicalArea> search(String country_uri, String modelType, String lang, String name, int start, int limit) {

        // This method isn't the nicest to read ever, so here's what it does:
        // 1. tokenize the input and search the cache for each token in the input
        // 2. flatten the results into one big stream
        // 3. reduce the stream into a list of pairs <Area, Frequency>
        // 4. sort the stream in reverse frequency order so that results that match multiple query terms come first
        // 5. turn this back into a stream of areas and return

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
                float l = (float) area.getNameTokens().stream().count();
                float v = queryMatches / Math.max((float) queryTerms, l);
                return v;
            }
        }

        boolean atLeastOneLongToken = false;
        List<String> queryTerms = tokenizer.tokenize(name).stream().collect(Collectors.toList());
        for (String token : queryTerms) {
            if (token.length() >= 3) {
                atLeastOneLongToken = true;
            }
        }
        if (!atLeastOneLongToken || queryTerms.isEmpty()) {
            return (new ArrayList<GeographicalArea>()).stream();
        }

        LinkedHashMap<String, Pair> collected = findOneToken(country_uri, new String[]{modelType}, lang, queryTerms.toArray(new String[queryTerms.size()])) // note that findOneToken doesn't allow duplicate URIs in results
            // ordering by number of hits :
            // (not used anymore since findOneToken() is only called once now,
            // but still required to deduplicate exact token match & actual fulltext queries,
            // though that could be done in a simpler way)
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

    /**
     * Search in local DB, filtering the results using the parameters
     *
     * @param countryUri URI of DC country Resource, therefore already encoded
     * @param nameTokens null to list all ex. geoco:Country_0
     */
    public Stream<GeographicalArea> findOneToken(String countryUri, String[] modelTypes, String lang, String[] nameTokens) {
        // looking for exact token matches first :
        // (otherwise Saint-Lô is hidden behind a lot of ex. Saint-Lormel)
        Query exactQuery = buildQuery(countryUri, modelTypes, lang, nameTokens, "$");
        List<GeographicalArea> foundExactAreas = mongoTemplate.find(exactQuery, GeographicalArea.class);
        if (foundExactAreas.size() == findOneTokenLimit) {
            // should not happen
            logger.warn("Hit findOneTokenLimit (so probably missing some results) on query " + exactQuery);
        }

        // actuall fulltext search next :
        Query query = buildQuery(countryUri, modelTypes, lang, nameTokens, "");
        List<GeographicalArea> foundAreas = mongoTemplate.find(query, GeographicalArea.class);
        if (foundAreas.size() == findOneTokenLimit) {
            // should not happen
            logger.warn("Hit findOneTokenLimit (so probably missing some results) on query " + query);
        }

        // merging both (deduplicated in search()'s Pair lambda above) :
        foundExactAreas.addAll(foundAreas);

        return foundExactAreas
            .stream()
            .map(DCUrlWrapper::new)
            .distinct()
            .map(DCUrlWrapper::unwrap);
    }


    private Query buildQuery(String countryUri, String[] modelTypes, String lang,
        String[] nameTokens, String regexSuffix) {
        if (regexSuffix == null) {
            regexSuffix = "";
        }

        // we search irrespective of the replication status, but we deduplicate based on DC Resource URI.
        // sort spec means we want older results first - so that incoming replicates are discarded as long as
        // there is an online entry
        Criteria criteria = where("lang").is(lang);

        if (countryUri != null && !countryUri.trim().isEmpty()) {
            criteria.and("country").is(countryUri); //filter by country
        }

        List<Criteria> andCriteria = new ArrayList<>();
        if (modelTypes != null && modelTypes.length != 0) {
            for (String modelType : modelTypes) {
                if (modelType != null && !modelType.trim().isEmpty()) {
                    andCriteria.add(where("modelType").in(modelType));
                }
            }
        }

        if (nameTokens != null) {
            for (String nToken : nameTokens) {
                if (nToken != null && !nToken.trim().isEmpty()) {
                    andCriteria.add(where("nameTokens").regex("^" + nToken + regexSuffix));
                }
            }
        }

        if (!andCriteria.isEmpty()) {
            criteria.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
        }
        return query(criteria)
            .with(new Sort(Sort.Direction.ASC, "name"))
            .with(new Sort(Sort.Direction.ASC, "replicationTime"))
            .limit(findOneTokenLimit); // limit to prevent too much performance-hampering object scanning
    }

    public void save(GeographicalArea area) {
        mongoTemplate.save(area);
    }

    public long deleteByStatus(GeographicalAreaReplicationStatus status) {
        return mongoTemplate.remove(query(where("status").is(status)), GeographicalArea.class).getDeletedCount();
    }

    /**
     * Switch all "Incoming" entries to "Online"
     */
    public void switchToOnline() {
        mongoTemplate.updateMulti(
            query(where("status").is(GeographicalAreaReplicationStatus.INCOMING)),
            Update.update("status", GeographicalAreaReplicationStatus.ONLINE),
            GeographicalArea.class
        );
    }

    @PostConstruct
    public void initGeoCache() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.afterPropertiesSet();
        threadPoolTaskScheduler.schedule(() -> {
            if (mongoTemplate.getCollection("geographical_area").count() == 0) {
                logger.info("Geo area cache is empty, initializing it");
                replicate();
            }
        }, new Date());
    }

    @Scheduled(cron = "${application.geoarea.replication}")
    public void replicate() {

        logger.info("Starting replication of geographical data from data core");

        Set<String> loadedUris = new HashSet<>();

        // 0. remove all pre-existing INCOMING records created but never changed to ONLINE
        deleteByStatus(GeographicalAreaReplicationStatus.INCOMING);

        // 1. fetch all the resources from the data core and insert them with status "incoming" in the cache
        try {
            //Since there is not admin user connected, is necessary to get its admin authorization object in order to send the request
            systemUserService.runAs(() -> {
                String lastDCIdFetched = null;
                do {
                    logger.debug("Fetching batches of areas");
                    lastDCIdFetched = fetchBatches(loadedUris, lastDCIdFetched);
                } while (lastDCIdFetched != null);
            });


            // 2. delete all the "online" entries (they are replaced with the "incoming" ones)
            long deleteStart = System.currentTimeMillis();
            deleteByStatus(GeographicalAreaReplicationStatus.ONLINE);
            logger.debug("Deleted online data in {} ms", System.currentTimeMillis() - deleteStart);

            // 3. switch all "incoming" entries to "online"
            long switchStart = System.currentTimeMillis();
            this.switchToOnline();
            logger.debug("Switched to online in {} ms", System.currentTimeMillis() - switchStart);
            logger.info("Finish replication of {} geographical records from data core", mongoTemplate.getCollection("geographical_area").count());
        } catch (RestClientException e) {
            logger.error("Error while updating the geo area cache", e);

            // "rollback" to avoid duplicates on search (currently NOT filtered by the Status)
            this.deleteByStatus(GeographicalAreaReplicationStatus.INCOMING);
        }

    }

    private String fetchBatches(Set<String> loadedUris, String lastDCIdFetched) {

        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, "geographical_area");
        String prevDcId = lastDCIdFetched;

        DCQueryParameters params;
        params = lastDCIdFetched == null
            ? new DCQueryParameters(searchCronField, DCOrdering.DESCENDING)
            : new DCQueryParameters(searchCronField, DCOrdering.DESCENDING, DCOperator.LT, "\"" + lastDCIdFetched + "\"");
        // (LT & descending order to leave possible null geo:name values at the end rather than having to skip them)

        logger.debug("Querying the Data Core");
        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(project, areaModel, params, 0, batchSize);
        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd - queryStart);

        boolean hasOne = false;

        List<GeographicalArea> geographicalAreas = new ArrayList<>();
        for (DCResource res : resources) {
            String name = null;

            for (Languages language : Languages.values()) {

                GeographicalArea area = geographicalDAO.toGeographicalArea(res, language.getLanguage());
                if (area == null) {
                    continue;
                }

                if (!loadedUris.contains(language.getLanguage() + area.getUri())) {
                    hasOne = true;
                    if (name == null) {
                        name = area.getName();
                        //logger.debug("{} - {}", name, area.getUri());
                    }

                    geographicalAreas.add(area);
                    loadedUris.add(language.getLanguage() + area.getUri());
                } else {
                    if (name == null) {
                        name = area.getName();
                        logger.debug("Area {} already inserted for language {}, skipping", area.getName(), language.getLanguage());
                    }
                }
            }

            String id = res.getUri(); //  ID resource in DC is always encoded, so to match values we need to encoded as well
            if (id != null) {
                lastDCIdFetched = id;
            }

        }

        if (hasOne) {
            long st = System.currentTimeMillis();
            bulkOperations.insert(geographicalAreas);
            bulkOperations.execute();
            long durationSave = System.currentTimeMillis() - st;
            logger.debug("Saved resources; total save time={} ms (avg = {} ms)", durationSave, durationSave / resources.size());
        }

        if ((prevDcId != null && prevDcId.equals(lastDCIdFetched)) || resources.size() < batchSize) {
            return null;
        } else return lastDCIdFetched;
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
            return this.area.getUri().equals(((DCUrlWrapper) other).area.getUri());
        } else return false;
    }

    @Override
    public int hashCode() {
        return area.getUri().hashCode();
    }
}
