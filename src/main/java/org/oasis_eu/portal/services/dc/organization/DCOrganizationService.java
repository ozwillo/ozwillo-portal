package org.oasis_eu.portal.services.dc.organization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.oasis_eu.portal.core.exception.EntityNotFoundException;
import org.oasis_eu.portal.services.PortalSystemUserService;
import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.*;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.DCOrganizationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: Ignacio
 * Date: 7/2/15
 */

@Service
public class DCOrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(DCOrganizationService.class);

    @Autowired
    private DatacoreClient datacore;
    @Autowired
    private PortalSystemUserService portalSystemUserService;

    @Value("${application.dcOrg.project: org_0}")
    private String dcOrgProjectName;// = "org_0";

    @Value("${application.dcOrg.orgModel: org:Organization_0}")
    private String dcOrgModel;// = "org:Organization_0";

    @Value("${application.dcOrg.baseUri: http://data.ozwillo.com/dc/type}")
    private String dcBaseUri;// = "http://data.ozwillo.com/dc/type";

    @Value("${application.dcOrgSearch.sector: org:sector }")
    private String dcOrgSearchSector;
    @Value("${application.dcOrgSearch.legalName: org:legalName.v}")
    private String dcOrgSearchLegalName;
    @Value("${application.dcOrgSearch.regNumber: org:regNumber}")
    private String dcOrgSearchRegNumber;
    @Value("${application.dcOrgSearch.country: adrpost:country}")
    private String dcOrgSearchCountry;
    @Value("${application.dcOrgSearch.useTypeAsModel:false}")
    private boolean useTypeAsModel;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MessageSource messageSource;

    public DCOrganization searchOrganization(String lang, String country_uri, String sector, String regNumber) {

        DCResource resource = fetchDCOrganizationResource(country_uri, sector, regNumber);
        return resource != null ? toDCOrganization(resource, lang) : null;
    }

    public List<DCOrganization> searchOrganizations(String lang, String country_uri, String query) {
        String model = dcOrgModel.trim();
        String project = dcOrgProjectName.trim();

        DCQueryParameters params = new DCQueryParameters()
            .and(dcOrgSearchLegalName.trim(), DCOperator.FULLTEXT, query)
            //.and("odisp:name.v", DCOperator.FULLTEXT, query)
            .and(dcOrgSearchCountry.trim(), DCOperator.EQ, country_uri); /* country is encoded as a dc-resource, it should be sent also as it was fetched */

        logger.info("Searching organizations in DC using parameters : {}, {} and {}", query, country_uri, model);
        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(project, model, params, 0, 10);
        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd - queryStart);

        return resources.stream().map(resource -> toDCOrganization(resource, lang)).collect(Collectors.toList());
    }

    private DCResource fetchDCOrganizationResource(String country_uri, String sector, String regNumber) {
        String model = dcOrgModel.trim();
        String project = dcOrgProjectName.trim();

        DCQueryParameters params = new DCQueryParameters()
            .and(dcOrgSearchRegNumber.trim(), DCOperator.EQ, regNumber)
            .and(dcOrgSearchCountry.trim(), DCOperator.EQ, country_uri); /* country is encoded as a dc-resource, it should be sent also as it was fetched */

        logger.info("Searching resource in DC using parameters : {}, {} and {}", regNumber, country_uri, model);
        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(project, model, params, 0, 1);
        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd - queryStart);

        return this.processResourcesBySectorType(resources, sector);
    }

    /**
     * Filter a DCResource list with a valid sector type (accept only resource with no sector, or r.sector ==> param_sector), and remove spaces from found sector in resource.
     * <br>It also set the param_sector into the resource sector in case it is not found in resource.
     *
     * @param resources
     * @param sector
     * @return returns only the first element (DCResource) in resources list
     */
    private DCResource processResourcesBySectorType(List<DCResource> resources, String sector) {
        resources.forEach(r -> { // remove side spaces
            String rSector = r.getAsString("org:sector");
            if (rSector != null) {
                r.set("org:sector", rSector.trim());
            }
        });

        resources.stream().filter(r -> r.getAsString("org:sector") == null // sector not known yet (none or null valued)
            || r.getAsString("org:sector").equals(sector))
            .collect(Collectors.toList());

        if (resources.isEmpty()) {
            return null;
        }

        DCResource orgResource = resources.get(0);
        if (orgResource.getAsString("org:sector") == null) {
            orgResource.set("org:sector", sector); // sets sector if not yet known
        }
        return orgResource;
    }

    public DCOrganization searchOrganizationById(String dc_id, String language) {
        DCQueryParameters params = new DCQueryParameters("@id", DCOperator.EQ, dc_id);

        String model = dcOrgModel.trim();

        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(dcOrgProjectName.trim(), model, params, 0, 1);

        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd - queryStart);

        return resources.isEmpty() ? null : toDCOrganization(resources.get(0), language);
    }

    public Optional<DCResource> findOrganizationByCountryAndRegNumber(String countryUri, String regNumber) {

        DCResult result = datacore.getResourceFromURI(dcOrgProjectName.trim(), generateDcId(countryUri, regNumber));
        if (result.getType().equals(DCResultType.SUCCESS))
            return Optional.of(result.getResource());
        else
            return Optional.empty();
    }

    public Optional<DCOrganization> findOrganizationById(String dcId, String language) {
        DCResult result = datacore.getResourceFromURI(dcOrgProjectName.trim(), dcId);
        if (result.getType().equals(DCResultType.SUCCESS))
            return Optional.of(toDCOrganization(result.getResource(), language));
        else
            return Optional.empty();
    }

    public List<String> getOrganizationAliases(String dcId) {
        DCResult result = datacore.getResourceFromURI(dcOrgProjectName.trim(), dcId);
        if (!result.getType().equals(DCResultType.SUCCESS)) {
            logger.error("Unable to retrieve resource {}", dcId);
            throw new EntityNotFoundException();
        }

        DCResource orgResource = result.getResource();
        return datacore.getResourceAliases(dcOrgProjectName.trim(), orgResource.getType(), orgResource.getIri());
    }

    /**
     * Creates a new DC Organization. It fetch last DC org, then merge it with current data, update it in DC OR create it if it doesn't exist
     *
     * @param dcOrganization
     * @return DCResource
     * @throws ForbiddenException if operation not authorized by Datacore, probably because
     *                            user organizations have been changed see #264 & DC#100
     */
    public DCResource create(DCOrganization dcOrganization) throws ForbiddenException {
        // re-get DC resource before creation to validate that it doesn't exist
        String dcId = dcOrganization.getId() != null ? dcOrganization.getId() :
            generateDcId(dcOrganization.getCountry_uri(), dcOrganization.getTax_reg_num());
        DCResource dcResource = datacore.getResourceFromURI(dcOrgProjectName.trim(), dcId).getResource();


        // if found check that version hasn't changed since filling the form (i.e. since clicking on "search")
        if (dcResource != null && dcResource.getVersion() != Integer.parseInt(dcOrganization.getVersion())) {
            String translatedBusinessMessage = messageSource.getMessage("my.network.organization.error.already_exist",
                    new Object[]{}, RequestContextUtils.getLocale(request));
            throw new WrongQueryException(translatedBusinessMessage, HttpStatus.BAD_REQUEST.value());
        }

        if (dcResource != null) { //found in DC
            logger.debug("It exists, and there are no previous updates. Merging it from form fields, and doing a datacoreClient.updateResource()");
            mergeDCOrgToDCResources(dcOrganization, dcResource);
            DCResult dcResult = datacore.updateResource(dcOrgProjectName.trim(), dcResource); // to test locally, you must change url as datacore namespace (plnm-dev-dc)

            if (dcResult.getErrorMessages() != null && !dcResult.getErrorMessages().isEmpty()) {
                dcResult.getErrorMessages().forEach(logger::info);
                throw new WrongQueryException(HttpStatus.BAD_REQUEST.value());
            }

            return dcResource;
        } else {  // still doesn't exist in DC
            logger.debug("It doesn't exist in DC Doing a datacore.saveResource() with : {},{}", dcOrgProjectName.trim(), dcOrganization);
            DCResult newCreatedDCRes = datacore.saveResource(dcOrgProjectName.trim(), toNewDCResource(dcOrganization));

            if (newCreatedDCRes.getResource() == null) {
                newCreatedDCRes.getErrorMessages().forEach(logger::info);
                throw new WrongQueryException(HttpStatus.BAD_REQUEST.value());
            }

            logger.debug("Setting the new DC URI into the dcOrganization : {}", newCreatedDCRes.getResource().getUri());
            dcOrganization.setId(newCreatedDCRes.getResource().getUri());
            return newCreatedDCRes.getResource();
        }
    }

    /**
     * Update DC Organization data re-using this.create(DCOrganization) method.
     */
    public DCResource update(DCOrganization dcOrganization) {
        return this.create(dcOrganization);
    }

    /**
     * Change rights of DC Organization.
     */
    public boolean changeDCOrganizationRights(DCResource dcResource, String kOrgId) {
        final List<String> newRights = new ImmutableList.Builder<String>().add(kOrgId).build();
        final List<String> dcResultErrOutter = new ArrayList<>();

        //get admin authentication and change organization rights
        portalSystemUserService.runAs(
            () -> {
                DCRights dcRights = datacore.getRightsOnResource(dcOrgProjectName, dcResource).getRights();
                dcRights.addOwners(newRights);
                List<String> dcResultInner = datacore.setRightsOnResource(dcOrgProjectName, dcResource, dcRights).getErrorMessages();
                dcResultErrOutter.addAll(dcResultInner);
                if (dcResultInner != null && !dcResultInner.isEmpty()) {
                    logger.error("There was an error while updating rights [{}] to resource : {}", dcRights, dcResource);
                }
            }
        );
        return dcResultErrOutter.isEmpty();
    }


    public List<DCRegActivity> searchTaxRegActivity(String countryUri, String queryTerms, int start, int limit) {
        return fetchResourceByCountryAndNameStartingWith(queryTerms, "orgact:code", null, countryUri, "orgact:country", dcOrgProjectName, "orgact:Activity_0", limit - start)
            .stream().map(resource -> toDCRegActivity(resource))
            .collect(Collectors.toList());
    }


    // ******************* Helper methods *****************************************

    /**
     * Search values in DC, filtering by country
     *
     * @param queryTerm
     * @param field
     * @param subField
     * @param countryUri   (already encoded)
     * @param countryField
     * @param projectName
     * @param modelName
     * @param batchSize
     * @return
     */
    private List<DCResource> fetchResourceByCountryAndNameStartingWith(String queryTerm, String field, String subField,
        String countryUri, String countryField, String projectName, String modelName, int batchSize) {

        DCQueryParameters params = new DCQueryParameters();
        if (queryTerm != null && !queryTerm.trim().isEmpty()) {
            params.and(field.trim() + (subField == null ? "" : subField), DCOperator.EQ, DCOperator.REGEX.getRepresentation() + "^" + queryTerm);
        } else {
            params.and(field.trim(), DCOrdering.DESCENDING);
        }

        String encodedCountryUri = countryUri;
        try {
            if (encodedCountryUri != null && !encodedCountryUri.isEmpty()) {
                //encodedCountryUri  = UriComponentsBuilder.fromUriString(countryUri).build().encode().toString();
                encodedCountryUri = countryUri;
                params.and(countryField.trim(), DCOperator.EQ, encodedCountryUri);
            }
        } catch (Exception e) {
            logger.debug("The country URI \"{}\" cannot be encoded : {}", countryUri, e.toString());
        }

        logger.debug("Querying the Data Core with : country \"{}\"/encoded \"{}\", and terms {}", countryUri, encodedCountryUri, queryTerm);
        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(projectName.trim(), modelName.trim(), params, 0, batchSize);
        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd - queryStart);

        return resources;
    }

    private DCResource mergeDCOrgToDCResources(DCOrganization fromOrg, DCResource toRes) {
        // Organization data
        toRes.setMappedList("org:legalName", valueAsDCList(fromOrg.getLegal_name(), fromOrg.getLang())); //list
        toRes.setMappedList("odisp:name", valueAsDCList(
            fromOrg.getLegal_name() + "," + fromOrg.getCity() + " - " + fromOrg.getCountry()
            , fromOrg.getLang())); //list - also find as org:displayName

        DCOrganizationType dcOrganizationType = DCOrganizationType.getDCOrganizationType(fromOrg.getSector_type());
        toRes.set("org:sector", (dcOrganizationType != null) ? dcOrganizationType.name() : "");
        // only set org:country for :
        //   - newly created organizations (it is an immutable field)
        //   - previously created organizations without this field (see https://github.com/ozwillo/ozwillo-portal/issues/363)
        if (toRes.getAsString("org:country") == null || toRes.getAsString("org:country").isEmpty())
            toRes.set("org:country", fromOrg.getCountry_uri());

        toRes.set("org:status", fromOrg.isIn_activity() ? dcOrgStatusActive : dcOrgStatusInactive);
        toRes.setMappedList("org:altName", valueAsDCList(fromOrg.getAlt_name(), fromOrg.getLang())); //list
        toRes.set("org:type", fromOrg.getOrg_type());
        toRes.set("org:regNumber", fromOrg.getTax_reg_num());
        toRes.set("orgpu:officialId", fromOrg.getTax_reg_official_id()); /* Only for Public organizations*/
        toRes.set("org:activity", fromOrg.getTax_reg_activity_uri());
        if (dcOrganizationType == DCOrganizationType.Public) {
            toRes.set("orgpu:jurisdiction", fromOrg.getJurisdiction_uri()); /* Only for Public organizations*/
        }
        toRes.set("org:phoneNumber", fromOrg.getPhone_number());
        toRes.set("org:webSite", fromOrg.getWeb_site());
        toRes.set("org:email", fromOrg.getEmail());
        // Geolocation data
        toRes.set("adrpost:streetAndNumber", fromOrg.getStreet_and_number());
        toRes.set("adrpost:POBox", fromOrg.getPo_box());
        toRes.set("adrpost:postName", fromOrg.getCity_uri());
        toRes.set("adrpost:postCode", fromOrg.getZip());
        toRes.set("adrpost:cedex", fromOrg.getCedex());
        toRes.set("adrpost:country", fromOrg.getCountry_uri());

        //toRes.set("org:latitude", fromOrg.getLatitude());   //use once mapping localization is ready
        //toRes.set("org:longitude", fromOrg.getLongitude()); //use once mapping localization is ready

        //toRes.setLastModified(ZonedDateTime.now().toInstant());

        return toRes;
    }

    private List<Map<String, DCResource.Value>> valueAsDCList(String value, String language) {
        Map<String, DCResource.Value> myMap = new HashMap<>(2);
        myMap.put("@language", new DCResource.StringValue(language));
        myMap.put("@value", new DCResource.StringValue(value));

        List<Map<String, DCResource.Value>> legalNameLst = new ArrayList<>(1);
        legalNameLst.add(myMap);
        return legalNameLst;
    }

    private DCResource toNewDCResource(DCOrganization dcOrganization) {
        DCResource dcResource = mergeDCOrgToDCResources(dcOrganization, new DCResource());

        dcResource = setDCIdOrganization(dcResource, dcOrganization.getTax_reg_num(), dcOrganization.getCountry_uri());

        return dcResource;
    }

    public DCResource setDCIdOrganization(DCResource dcResource, String regNumber, String country_uri) {
        //"@id" : "http://data.ozwillo.com/dc/type/orgfr:Organization_0/FR/47952557800049",
        String countryAcronym = getCountryAcronym(country_uri);
        String cx = countryAcronym.toLowerCase(); //could throw NullPointerException when country is not provided
        String orgModelType = generateResourceType(country_uri);

        dcResource.setBaseUri(dcBaseUri.trim());
        dcResource.setType(orgModelType);
        // country acronym has no characters requiring encoding
        dcResource.setIri(cx.toUpperCase()
            + "/" + DCResource.encodeUriPathSegment(regNumber));

        return dcResource;
    }

    public String generateResourceType(String country_uri) {
        String countryAcronym = getCountryAcronym(country_uri);
        String cx = countryAcronym.toLowerCase(); //could throw NullPointerException when country is not provided

        String orgModelPrefix = "org" + cx;
        String orgModelSuffix = dcOrgPrefixToSuffix.get(orgModelPrefix);
        return orgModelPrefix + ":" + orgModelSuffix + "_0";
    }

    public String getCountryAcronym(String country_uri) {
        //Assumes that all the country have the abbreviation at the end of URL : http://data.ozwillo.com/dc/type/geocotr:%C3%9Clke_0/TR
        if (country_uri != null && !country_uri.isEmpty()) {
            return country_uri.substring(country_uri.length() - 2);
        }
        return null;
    }

    private static final Map<String, String> dcOrgPrefixToSuffix = new ImmutableMap.Builder<String, String>()
        //.put("org", "Organisation") //org:Organisation_0 when there is no country defined (but it neve should happen)
        .put("orgfr", "Organisation")
        .put("orgbg", "Организация")
        .put("orgit", "Organizzazione")
        .put("orgtr", "Organizasyon")
        .put("orges", "Organización")
        .build();

    private static final String dcOrgStatusActive = "Normal Activity";
    private static final String dcOrgStatusInactive = "Inactive";
    private static final List<String> dcOrgStatusInactiveList = new ImmutableList.Builder<String>()
        .add(dcOrgStatusInactive) // translation of portal's "false"
        // original list of inactive statuses :
        .add("Insolvent")
        .add("Bankrupt")
        .add("In Receivership")
        .build();

    public DCOrganization toDCOrganization(DCResource res, String language) {

        String legalName = getBestI18nValue(res, language, "org:legalName", null); //Mapped list

        String in_activity_val = getBestI18nValue(res, language, "org:status", null);
        boolean in_activity = (in_activity_val != null && dcOrgStatusActive.equals(in_activity_val));

        String sector = getBestI18nValue(res, language, "org:sector", null);
        String altName = getBestI18nValue(res, language, "org:altName", null); //Mapped list

        String org_type = getBestI18nValue(res, language, "org:type", null);

        String taxRegAct_uri = getBestI18nValue(res, language, "org:activity", null);
        String taxRegAct = getRemoteBestI18nValue(taxRegAct_uri, dcOrgProjectName, language, "orgact:code", null);

        String officialId = getBestI18nValue(res, language, "orgpu:officialId", null);
        String regNumber = getBestI18nValue(res, language, "org:regNumber", null);

        String jurisdiction_uri = getBestI18nValue(res, language, "orgpu:jurisdiction", null);
        String jurisdiction = getRemoteBestI18nValue(jurisdiction_uri, dcOrgProjectName, language, "odisp:name", null);

        String phoneNumber = getBestI18nValue(res, language, "org:phoneNumber", null);
        String webSite = getBestI18nValue(res, language, "org:webSite", null);
        String email = getBestI18nValue(res, language, "org:email", null);

        // Geolocation data
        String streetAndNumber = getBestI18nValue(res, language, "adrpost:streetAndNumber", null);
        String POBox = getBestI18nValue(res, language, "adrpost:POBox", null);
        String city_uri = getBestI18nValue(res, language, "adrpost:postName", null);
        String city = getRemoteBestI18nValue(city_uri, dcOrgProjectName, language, "odisp:name", null);

        String zip = getBestI18nValue(res, language, "adrpost:postCode", "org:postCode");
        String cedex = getBestI18nValue(res, language, "adrpost:cedex", null);

        String country_uri = getBestI18nValue(res, language, "adrpost:country", null);
        String country = getRemoteBestI18nValue(country_uri, dcOrgProjectName, language, "geo:name", null);

        //String longitude=	 getBestI18nValue(res, "org:longitude", null);
        //String latitude =	 getBestI18nValue(res, "org:latitude", null);

        DCOrganization dcOrg = new DCOrganization();
        dcOrg.setLegal_name(legalName);
        dcOrg.setIn_activity(in_activity);
        dcOrg.setSector_type(sector);
        dcOrg.setAlt_name(altName);
        dcOrg.setOrg_type(org_type);

        dcOrg.setTax_reg_num(regNumber);                // tax Id number / N SIRET
        dcOrg.setTax_reg_official_id(officialId);       // localTaxId / INSEE	/* Only for public organizations from FR and TR*/
        dcOrg.setTax_reg_activity_uri(taxRegAct_uri);
        dcOrg.setTax_reg_activity(taxRegAct);  // NACE/NAF code

        dcOrg.setJurisdiction_uri(jurisdiction_uri); /* Only for public organizations*/
        dcOrg.setJurisdiction(jurisdiction); /* Only for public organizations*/

        dcOrg.setPhone_number(phoneNumber);
        dcOrg.setWeb_site(webSite);
        dcOrg.setEmail(email);

        dcOrg.setStreet_and_number(streetAndNumber);
        dcOrg.setPo_box(POBox);
        dcOrg.setCedex(cedex);
        dcOrg.setCity_uri(city_uri);
        dcOrg.setCity(city);
        dcOrg.setZip(zip);
        dcOrg.setCountry_uri(country_uri);
        dcOrg.setCountry(country);

        dcOrg.setId(res.getUri());
        dcOrg.setExist(true); // Organization was found !
        dcOrg.setLang(language);
        dcOrg.setVersion(res.getVersion() + "");

        return dcOrg;
    }

    /**
     * Return the resource value, first matching the fieldName, if not found then match with the altFieldName.
     * In case a Listed Map is found, the inner values are matched using i18n key @language, and @value.
     *
     * @param resource
     * @param language
     * @param fieldName
     * @param altFieldName
     * @return String with found value, null if not value was found (empty counts as not found value)
     */
    @SuppressWarnings("unchecked")
    private String getBestI18nValue(DCResource resource, String language, String fieldName, String altFieldName) {
        if (resource == null) {
            return null;
        }
        Object object = resource.get(fieldName);
        if (object == null && (altFieldName != null && !altFieldName.isEmpty())) {
            logger.warn("Field \"{}\" not found. Fallback using field name \"{}\"", fieldName, altFieldName);
            object = resource.get(altFieldName);
        }
        if (object == null) { // if after double matched is not found then return null
            logger.warn("DC Resource {} of type {} has no the required field \"{}\" nor alt field \"{}\"",
                resource.getUri(), resource.getType(), fieldName, altFieldName);
            return null;
        }
        // Parse the list checking if it's a simple list or a listed map
        if (object instanceof List) {
            String valueMap = null;
            for (Object obj : (List<Object>) object) {
                if (obj instanceof Map) {
                    Map<String, String> nameMap = (Map<String, String>) obj;
                    logger.debug("nameMap: " + nameMap.toString());
                    String l = nameMap.get("l") != null ? nameMap.get("l") : nameMap.get("@language");
                    if (l == null) {
                        continue; /* shouldn't happen */
                    }
                    if (l.equalsIgnoreCase(language)) {
                        String val = nameMap.get("v") != null ? nameMap.get("v") : nameMap.get("@value");
                        return val == null || val.isEmpty() ? null : val; //break; // can't find better
                    }
                    if (valueMap == null) { // takes the last valid match
                        valueMap = nameMap.get("v") != null ? nameMap.get("v") : nameMap.get("@value");
                    }
                } else {
                    valueMap = object.toString();
                } // Its a list of strings //TODO use it and test it
            }
            return valueMap;
        } else if (object instanceof String) {
            String val = (String) object;
            return val == null || val.isEmpty() ? null : val;
        } else {
            logger.warn("No value was found in mapped list using language \"{}\" nor as a string field.", language);
        }

        return null;
    }

    /**
     * Return the resource value, first fetch the resource in the passed parameter,
     * then it matches the said resource with the fieldName, if not found then match with the altFieldName.
     * In case a Listed Map is found, the inner values are matched using i18n key @language, and @value.
     *
     * @param resource_uri
     * @param dcOrgProjectName
     * @param language
     * @param fieldName
     * @param altFieldName
     * @return String with found value, null if not value was found (empty counts as not found value)
     */
    private String getRemoteBestI18nValue(String resource_uri, String dcOrgProjectName, String language, String fieldName,
        String altFieldName) {

        if (resource_uri == null || resource_uri.length() == 0) {
            logger.warn("Resource URI is null or empty. No {} nor {} value will be fetched.", fieldName, altFieldName);
            return null;
        }

        DCResult dcResult = datacore.getResourceFromURI(dcOrgProjectName, resource_uri);
        if (dcResult.getType() == DCResultType.SUCCESS) {
            DCResource dcResource = dcResult.getResource();
            String i18nValue = getBestI18nValue(dcResource, language, fieldName, altFieldName);
            return i18nValue;
        } else {
            logger.error("Got an unsuccessful response from Datacore while fetching the ressource \"{}\". Error:\"{}\", Message:{}",
                resource_uri, dcResult.getType(), dcResult.getErrorMessages());
        }
        return null;
    }

    /**
     * TODO move to OrgDAO / OrganizationDCResourceHelper class
     */
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

    public String generateDcId(String countryUri, String regNumber) {
        String type = generateResourceType(countryUri);
        String iri = getCountryAcronym(countryUri).toUpperCase() + '/' + regNumber;
        return dcBaseUri.trim() + '/' + type + '/' + iri;
    }
}
