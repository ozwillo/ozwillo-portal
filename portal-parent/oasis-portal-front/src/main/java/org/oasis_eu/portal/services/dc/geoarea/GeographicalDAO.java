package org.oasis_eu.portal.services.dc.geoarea;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.oasis_eu.portal.core.mongo.dao.geo.GeographicalAreaCache;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalArea;
import org.oasis_eu.portal.core.services.search.Tokenizer;
import org.oasis_eu.portal.services.dc.organization.DCRegActivity;
import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCOperator;
import org.oasis_eu.spring.datacore.model.DCOrdering;
import org.oasis_eu.spring.datacore.model.DCQueryParameters;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * TODO #252 move generic & org stuff outside, better Service vs DAO archi
 * 
 * TODO LATER #252 make generic DatacoreBusinessFulltextCache/Dao<BO> (business object)
 * and concretize GeographicalAreaFulltextCache extends DatacoreBusinessFulltextCache<GeographicalArea>
 * created in its own @Configuration class see http://stackoverflow.com/questions/11845871/spring-value-annotation-inheritance-and-common-fields-but-different-values
 *
 * User: Ignacio
 * Date: 7/2/15
 */
@Component
public class GeographicalDAO {

    private static final Logger logger = LoggerFactory.getLogger(GeographicalDAO.class);

    @Autowired
    private DatacoreClient datacore;

    /** geo_0/1... can also be used to use a version not yet published (i.e. made visible in geo) */
    @Value("${application.geoarea.project:geo}")
    private String geoProject;
    /** org_0/1... can also be used to use a version not yet published (i.e. made visible in org) */
    @Value("${application.dcOrg.project:org}")
    private String dcOrgProjectName;

    /**ex. Rhône-Alpes */
    @Value("${application.geoarea.nameField:geo:name}")
    private String nameField; // or city specific "geoci:name", country-specific "geoco:name"
    /** ex. Rhône-Alpes, France */
    @Value("${application.geoarea.displayNameField:odisp:name}")
    private String displayNameField;// or geo specific "geo:displayName"

    @Value("${application.geoarea.countryModel:geoco:Country_0}")
    private String countryModel; // or country specific "geocofr:Pays_0"
    @Value("${application.geoarea.cityModel:geoci:City_0}")
    private String cityModel;
    @Value("${application.geoarea.countryField:geo:country}")
    private String countryField; // or city specific "geoci:country"

    @Autowired
    private Tokenizer tokenizer;

    @Autowired
    private GeographicalAreaCache geographicalAreaCache;

    // Cities
    public List<GeographicalArea> searchCities(String lang, String queryTerm, String countryUri, int start, int limit) {

       return fetchResourceByCountryAndNameStartingWith(queryTerm, nameField, ".v", countryUri, countryField, geoProject, cityModel, limit - start)
               .stream().map(resource -> toGeographicalArea(resource,lang))
               .collect(Collectors.toList());
    }

    // Countries
    public List<GeographicalArea> searchCountries(String lang, String term, int start, int limit) {
        return fetchResourceByCountryAndNameStartingWith(term, nameField, ".v", null,null, geoProject, countryModel, limit - start)
              .stream().map(resource -> toGeographicalArea(resource,lang))
              .collect(Collectors.toList());
    }

    // Tax Reg Activity
    public List<DCRegActivity> searchTaxRegActivity(String countryUri, String queryTerms, int start, int limit) {
        return fetchResourceByCountryAndNameStartingWith(queryTerms, "orgact:code", null, countryUri, "orgact:country", dcOrgProjectName, "orgact:Activity_0", limit - start)
               .stream().map(resource -> toDCRegActivity(resource))
               .collect(Collectors.toList());
    }



    // Helper & Handler methods

    /**
     * 
     * @param queryTerm
     * @param field
     * @param subField
     * @param countryUri (already encoded)
     * @param countryField
     * @param projectName
     * @param modelName
     * @param batchSize
     * @return
     */
    private List<DCResource> fetchResourceByCountryAndNameStartingWith(String queryTerm, String field, String subField,
            String countryUri, String countryField, String projectName, String modelName, int batchSize ){

        DCQueryParameters params = new DCQueryParameters();
        if(queryTerm != null && !queryTerm.trim().isEmpty()){
            params.and(field.trim()+(subField == null ? "" : subField), DCOperator.EQ, DCOperator.REGEX.getRepresentation()+"^"+queryTerm);
        } else { params.and(field.trim(), DCOrdering.DESCENDING); }

        String encodedCountryUri = countryUri;
        try{
            if(encodedCountryUri != null && !encodedCountryUri.isEmpty()){
                //encodedCountryUri  = UriComponentsBuilder.fromUriString(countryUri).build().encode().toString();
                encodedCountryUri  = countryUri;
                params.and(countryField.trim(), DCOperator.EQ, encodedCountryUri);
            }
        }catch(Exception e){
            logger.debug("The country URI \"{}\" cannot be encoded : {}", countryUri, e.toString());
        }

        logger.debug("Querying the Data Core with : country \"{}\"/encoded \"{}\", and terms {}", countryUri, encodedCountryUri, queryTerm );
        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(projectName.trim(), modelName.trim(), params, 0, batchSize);
        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd - queryStart);

        return resources;
    }

    /**
     * 
     * @param r
     * @param language
     * @return geo area, using geo:name as nameTokens and odisp:name as (displayed) name
     */
    public GeographicalArea toGeographicalArea(DCResource r, String language) {

        String displayName = getBestI18nValue(r, language, displayNameField, nameField);
        if (displayName == null) {
            logger.warn("DC Resource {} of type {} has no field named {}", r.getUri(), r.getType(), nameField);
            return null;
        }
        String name = getI18nValue(r, language, nameField);

        String country = r.getAsString(countryField);  /* The true value should be the main referenced model id (geo:country), but today
                                                           * it is not linked in the models. NB today it makes that some of the fields are not
                                                           * stored due to this field (been empty for those cases) */
        List<String> modelType = r.getAsStringList("@type");

        GeographicalArea area = new GeographicalArea();
        area.setName(displayName);
        area.setUri(r.getUri());
        area.setLang(language);
        area.setCountry(country);
        area.setModelType(modelType);

        area.setNameTokens(tokenizer.tokenize(name));

        try {
            List<String>  ancestors = r.getAsStringList("oanc:ancestors");
            area.setAncestors(ancestors);
        } catch (NullPointerException e) {
            logger.warn("The key 'oanc:ancestors' doesn't exist in the fetched resource.");
        }


        return area;
    }
    
    private String getI18nValue(DCResource r, String language, String i18nField) {
        String value = null;
        @SuppressWarnings("unchecked")
        List<Map<String, String>> valueMaps = (List<Map<String, String>>) r.get(i18nField);
        //logger.debug("valueMaps: " + valueMaps.toString());
        for (Map<String, String> valueMap : valueMaps) {
            String l = valueMap.get("@language"); // TODO Q why ?? @language only in application/json+ld, otherwise l
            if (l == null) { continue; /* shouldn't happen */ }
            if (l.equals(language)) {
                value = valueMap.get("@value"); // TODO Q why ?? @value only in application/json+ld, otherwise v
                break; // can't find better
            }
            if (value == null) {
                value = valueMap.get("@value"); // TODO Q why ?? @value only in application/json+ld, otherwise v
            }
            //TODO LATER: Create a full body DC interceptor to test request/response to DATACORE (similar to KernelLoggingInterceptor)
        }
        return value;
    }

    /** TODO move to generic (-integration ? DCResource ??) 
     * @param nameField2 */
    private String getBestI18nValue(DCResource resource,
            String language, String fieldName, String altFieldName){
        @SuppressWarnings("unchecked")
        List<Map<String, String>> nameMaps = (List<Map<String, String>>) resource.get(fieldName);
        if( (nameMaps == null || nameMaps.isEmpty()) && (altFieldName != null && !altFieldName.isEmpty() ) ){
            logger.warn("Field \"{}\" not found. Fallback using field name \"{}\"", fieldName, altFieldName);
            return getI18nValue(resource, language, altFieldName);
        }
        return getI18nValue(resource, language, fieldName);
    }
    
    /** TODO move to OrgDAO */
    public DCRegActivity toDCRegActivity(DCResource r) {
        String code = r.getAsString("orgact:code");
        String country = r.getAsString("orgact:country");
        String label = r.getAsString("orgact:label");

        DCRegActivity activity = new DCRegActivity();
        activity.setName(code);
        activity.setLabel(label);
        activity.setCountry(country);
        activity.setUri(r.getUri());

        return activity;
    }

}
