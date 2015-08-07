package org.oasis_eu.portal.services.dc.geoarea;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.web.util.UriComponentsBuilder;

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

    // Cities
    public List<GeographicalArea> searchCities(String lang, String terms, String country, int start, int limit) {

        List<String> queryTerms = tokenizer.tokenize(terms, false, false).stream().filter(t -> t.length() >= 3).collect(Collectors.toList());

        List<GeographicalArea> geographicalArea = new ArrayList<GeographicalArea>();

        fetchDCCitiesResource(country, queryTerms, limit - start)
               .stream().forEach(resource -> geographicalArea.add(toGeographicalArea(resource,lang, displayNameField.trim(), cityField.trim())));

        return geographicalArea;
    }

    private List<DCResource> fetchDCCitiesResource(String country, List<String> queryTerms, int batchSize ) {
        // /dc/type/geo:City_0?geo_city:name.l=fr&geo_city:name.v=$regexAas
        String encodedCountry = country;
        try{
            encodedCountry = UriComponentsBuilder.fromUriString(country).build().encode().toString();
        }catch(Exception e){
            logger.debug("The country URI \"{}\" cannot be encoded : {}", country, e.toString());
        }

        DCQueryParameters params = !queryTerms.isEmpty()
                ? ( (encodedCountry != null && !encodedCountry.isEmpty()) ? new DCQueryParameters(countryCityField.trim(), DCOperator.EQ, encodedCountry) : new DCQueryParameters())
                      .and(cityField.trim()+".v", DCOperator.EQ, DCOperator.REGEX.getRepresentation()+queryTerms.get(0))
                : new DCQueryParameters(cityField.trim(), DCOrdering.DESCENDING);

        logger.debug("Querying the Data Core with : country \"{}\"/encoded \"{}\", and terms {}", country, encodedCountry, queryTerms );
        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(geoProject.trim(), geoCityModel.trim(), params, 0, batchSize);
        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd - queryStart);

        return resources;
    }

    // Tax Reg Activity
    public List<DCRegActivity> searchTaxRegActivity(String country, String queryTerms, int start, int limit) {

        List<DCRegActivity> taxRegActivities = new ArrayList<DCRegActivity>();

        fetchTaxRegActivityResource(country, queryTerms, limit - start)
               .stream().forEach(resource -> taxRegActivities.add(toDCRegActivity(resource, displayNameField.trim(), cityField.trim())));

        return taxRegActivities;
    }
    private List<DCResource> fetchTaxRegActivityResource(String country, String queryTerms, int batchSize ) {
        // /dc/type/geo:City_0?geo_city:name.l=fr&geo_city:name.v=$regexAas
        String encodedCountry = country;
        try{
            encodedCountry = UriComponentsBuilder.fromUriString(country).build().encode().toString();
        }catch(Exception e){
            logger.debug("The country URI \"{}\" cannot be encoded : {}", country, e.toString());
        }

        DCQueryParameters params = !queryTerms.isEmpty()
                ? ( (encodedCountry != null && !encodedCountry.isEmpty()) ? new DCQueryParameters("orgact:country", DCOperator.EQ, encodedCountry) : new DCQueryParameters())
                      .and("orgact:code", DCOperator.EQ, DCOperator.REGEX.getRepresentation()+queryTerms)
                : new DCQueryParameters("orgact:code", DCOrdering.DESCENDING);

        logger.debug("Querying the Data Core with : country \"{}\"/encoded \"{}\", and terms {}", country, encodedCountry, queryTerms );
        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(dcOrgProjectName.trim(), "orgact:Activity_0", params, 0, batchSize);
        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd - queryStart);

        return resources;
    }

    // Countries
    public List<GeographicalArea> searchCountries(String lang, String terms, int start, int limit) {

        List<String> queryTerms = tokenizer.tokenize(terms, false, false).stream().filter(t -> t.length() >= 3).collect(Collectors.toList());

        List<GeographicalArea> geographicalArea = new ArrayList<GeographicalArea>();

        List<DCResource> resources = fetchDCCountriesResource(lang, queryTerms, limit - start);

        resources.stream().forEach(resource -> geographicalArea.add(toGeographicalArea(resource,lang, displayNameField.trim(), countryField.trim())));

        return geographicalArea;
    }
    private List<DCResource> fetchDCCountriesResource(String lang, List<String> queryTerms, int batchSize ) {
        // 
        DCQueryParameters params = !queryTerms.isEmpty() 
                ? new DCQueryParameters(/*countryField.trim()+ ".l", DCOperator.EQ, lang*/)
                      .and(countryField.trim()+".v", DCOperator.EQ, DCOperator.REGEX.getRepresentation()+"^"+queryTerms.get(0))
                : new DCQueryParameters(countryField.trim(), DCOrdering.DESCENDING);

        logger.debug("Querying the Data Core");
        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(geoProject.trim(), geoCountriesModel.trim(), params, 0, batchSize);
        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd - queryStart);

        return resources;
    }


    // Helper & Handler methods
    public GeographicalArea toGeographicalArea(DCResource r, String language, String nameField, String altFieldName) {

        List<Map<String, String>> nameMaps = getBestI18nValue(r, nameField, altFieldName);
        if (nameMaps == null) {
            logger.warn("DC Resource {} of type {} has no field named {}", r.getUri(), r.getType(), nameField);
            return null;
        }
        String name = null;
        for (Map<String, String> nameMap : nameMaps) {
            logger.debug("nameMaps: " + nameMaps.toString());
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

        String country = r.getAsString("geoci:country");

        GeographicalArea area = new GeographicalArea();
        area.setName(name);
        area.setUri(r.getUri());
        area.setLang(language);
        area.setCountry(country);

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
