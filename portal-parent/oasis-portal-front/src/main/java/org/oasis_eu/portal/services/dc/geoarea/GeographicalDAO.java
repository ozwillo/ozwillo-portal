package org.oasis_eu.portal.services.dc.geoarea;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * User: Ignacio
 * Date: 7/2/15
 */
@Component
public class GeographicalDAO {

    private static final Logger logger = LoggerFactory.getLogger(GeographicalDAO.class);

    @Autowired
    private DatacoreClient datacore;

    @Value("${application.geoarea.project:geo_0}")
    private String geoProject;
    @Value("${application.dcOrg.project: org_0}")
    private String dcOrgProjectName;// = "org_0";

    @Value("${application.geoarea.geoCityModel: geo:City_0}")
    private String geoCityModel;// = "geoci:City_0";
    @Value("${application.geoarea.cityField: geoci_city:name}")
    private String cityField;// = "geo_city:name"; // "geoci:name";
    @Value("${application.geoarea.countryCityField: geoci:country}")
    private String countryCityField;// = "geoco:Country_0"; //"geocofr:Pays_0";
    
    @Value("${application.geoarea.geoCountriesModel: geoco:Country_0}")
    private String geoCountriesModel;// = "geoco:Country_0"; //"geocofr:Pays_0";
    /**ex. Rhône-Alpes */
    @Value("${application.geoarea.countryField: geoco:name}")
    private String countryField;// = "geoco:name"; // "geo:name";
    /** ex. Rhône-Alpes, France */
    @Value("${application.geoarea.displayNameField:odisp:name}")
    private String displayNameField;// = "odisp:name"; //geo:displayName

    @Autowired
    private Tokenizer tokenizer;

    @Autowired
    private GeographicalAreaCache geographicalAreaCache;

    // Cities
    public List<GeographicalArea> searchCities(String lang, String queryTerm, String countryUri, int start, int limit) {

        List<GeographicalArea> geographicalArea = new ArrayList<GeographicalArea>();

        fetchResourceByCountryAndNameStartingWith(queryTerm, cityField, ".v", countryUri, countryCityField, geoProject, geoCityModel, limit - start)
               .stream().forEach(resource -> geographicalArea.add(toGeographicalArea(resource,lang, displayNameField.trim(), cityField.trim())));

       return geographicalArea;
    }

    // Countries
    public List<GeographicalArea> searchCountries(String lang, String term, int start, int limit) {

        List<GeographicalArea> geographicalArea = new ArrayList<GeographicalArea>();

        fetchResourceByCountryAndNameStartingWith(term, countryField, ".v", null,null, geoProject,geoCountriesModel, limit - start)
            .stream().forEach(resource -> geographicalArea.add(toGeographicalArea(resource,lang, displayNameField.trim(), countryField.trim())));

        return geographicalArea;
    }

    // Tax Reg Activity
    public List<DCRegActivity> searchTaxRegActivity(String countryUri, String queryTerms, int start, int limit) {

        List<DCRegActivity> taxRegActivities = new ArrayList<DCRegActivity>();

        fetchResourceByCountryAndNameStartingWith(queryTerms, "orgact:code", null, countryUri, "orgact:country", dcOrgProjectName, "orgact:Activity_0", limit - start)
               .stream().forEach(resource -> taxRegActivities.add(toDCRegActivity(resource, displayNameField.trim(), cityField.trim())));

        return taxRegActivities;
    }



    // Helper & Handler methods

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

    public GeographicalArea toGeographicalArea(DCResource r, String language, String nameField, String altFieldName) {

        List<Map<String, String>> nameMaps = getBestI18nValue(r, nameField, altFieldName);
        if (nameMaps == null) {
            logger.warn("DC Resource {} of type {} has no field named {}", r.getUri(), r.getType(), nameField);
            return null;
        }
        String name = null;
        for (Map<String, String> nameMap : nameMaps) {
            //logger.debug("nameMaps: " + nameMaps.toString());
            String l = nameMap.get("@language"); // TODO Q why ?? @language only in application/json+ld, otherwise l
            if (l == null) { continue; /* shouldn't happen */ }
            if (l.equals(language)) {
                name = nameMap.get("@value"); // TODO Q why ?? @value only in application/json+ld, otherwise v
                break; // can't find better
            }
            if (name == null) {
                name = nameMap.get("@value"); // TODO Q why ?? @value only in application/json+ld, otherwise v
            }
            //TODO LATER: Create a full body DC interceptor to test request/response to DATACORE (similar to KernelLoggingInterceptor)
        }

        String country = r.getAsString("geo:country");  /* The true value should be the main referenced model id (geo:country), but today
                                                           * it is not linked in the models. NB today it makes that some of the fields are not
                                                           * stored due to this field (been empty for those cases) */
        List<String> modelType = r.getAsStringList("@type");

        GeographicalArea area = new GeographicalArea();
        area.setName(name);
        area.setUri(r.getUri());
        area.setLang(language);
        area.setCountry(country);
        area.setModelType(modelType);

        area.setNameTokens(tokenizer.tokenize(name));
        //area.setDetailedName(); // TODO fill in Datacore OR RATHER CACHE using names of NUTS3 or else 2 and country

        try {
            List<String>  ancestors = r.getAsStringList("oanc:ancestors");
            area.setAncestors(ancestors);
        } catch (NullPointerException e) {
            logger.warn("The key 'oanc:ancestors' doesn't exist in the fetched resource.");
        }


        return area;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> getBestI18nValue(DCResource resource, String fieldName, String altFieldName){
        List<Map<String, String>> nameMaps = (List<Map<String, String>>) resource.get(fieldName);
        if( (nameMaps == null || nameMaps.isEmpty()) && (altFieldName != null && !altFieldName.isEmpty() ) ){
            logger.warn("Field \"{}\" not found. Fallback using field name \"{}\"", fieldName, altFieldName);
            nameMaps = (List<Map<String, String>>) resource.get(altFieldName);
        }
        return nameMaps;
    }

    public DCRegActivity toDCRegActivity(DCResource r, String nameField, String altFieldName) {
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
