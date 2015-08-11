package org.oasis_eu.portal.services.dc.organization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oasis_eu.portal.services.PortalSystemUserService;
import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCOperator;
import org.oasis_eu.spring.datacore.model.DCQueryParameters;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.oasis_eu.spring.datacore.model.DCResult;
import org.oasis_eu.spring.datacore.model.DCRights;
import org.oasis_eu.spring.kernel.model.DCOrganizationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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

    public DCOrganization searchOrganization(String lang, String country_uri, String sector, String legalName, String regNumber) {

        DCOrganization dcOrganization = new DCOrganization();

        DCResource resource = fetchDCOrganizationResource(country_uri, sector, legalName, regNumber, lang);
        if(resource != null ){
            dcOrganization = toDCOrganization(resource,lang);
        }else{
            dcOrganization = new DCOrganization();
        }

        return dcOrganization;
    }

    private DCResource fetchDCOrganizationResource(String country_uri, String sector, String legalName, String regNumber, String lang) {
        DCQueryParameters params = new DCQueryParameters()
                      //.and(dcOrgSearchSector.trim(), DCOperator.EQ, sector)
                      //.and(dcOrgSearchLegalName.trim(), DCOperator.EQ, DCOperator.REGEX.getRepresentation()+legalName)
                      .and(dcOrgSearchRegNumber.trim(), DCOperator.EQ, regNumber)
                      .and(dcOrgSearchCountry.trim(), DCOperator.EQ, country_uri);

        String model = dcOrgModel.trim();
        // NT. Since typeAsModel requires sector and the sector is nor required to the search, useTypeAsModel should always be false;
        // otherwise, it will be implicitly in the query (e.g. dc/type/orgpuit:OrgPubblica_0/IT/05719580010)
        if(useTypeAsModel){ model = this.generateResourceType(sector, country_uri, regNumber); };

        logger.info("Ressource not found using parameters : {}, {} and {}", regNumber, country_uri, model);
        logger.debug("Querying the Data Core");
        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(dcOrgProjectName.trim(), model, params, 0, 1);
        /*if(resources ==null || resources.isEmpty()){ /TODO this is for TEST only
            //If is not found using all search factors, it re-search only by regNum
            resources = datacore.findResources(dcOrgProjectName.trim(), dcOrgModel.trim(), new DCQueryParameters("org:regNumber", DCOperator.EQ, regNumber), 0, 1);
        }*/
        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd-queryStart);

        return resources.isEmpty()? null : resources.get(0);
    }

    public DCOrganization searchOrganizationById(String dc_id){
        DCQueryParameters params = new DCQueryParameters("@id", DCOperator.EQ, dc_id);

        String model = dcOrgModel.trim();

        logger.info("Ressource not found using parameters : {}", dc_id);

        long queryStart = System.currentTimeMillis();
        List<DCResource> resources = datacore.findResources(dcOrgProjectName.trim(), model, params, 0, 1);

        long queryEnd = System.currentTimeMillis();
        logger.debug("Fetched {} resources in {} ms", resources.size(), queryEnd-queryStart);

        return resources.isEmpty()? null : toDCOrganization(resources.get(0),"FR");
    }

    public DCResource create(DCOrganization dcOrganization){
        // re-get DC resource before creation to validate that it doesn't exist
        DCResource dcResource = fetchDCOrganizationResource(dcOrganization.getCountry_uri(),
                DCOrganizationType.getDCOrganizationType(dcOrganization.getSector_type()).name(),
                dcOrganization.getLegal_name(),dcOrganization.getTax_reg_num(), dcOrganization.getLang());
        // if found check that version hasn't changed since filling the form (i.e. since clicking on "search"),
        if (dcResource != null && dcResource.getVersion() == Integer.parseInt(dcOrganization.getVersion()) ){ //found in DC
            logger.debug("It exists, but there are no previous updates. Merging it from form fields, and doing a datacoreClient.updateResource()");
            mergeDCOrgToDCResources(dcOrganization, dcResource);
            DCResult dcResult = datacore.updateResource(dcOrgProjectName.trim(), dcResource); // to test must change url as datacore namespace (plnm-dev-dc)
            return dcResult != null ? dcResource : null;
        }else if (dcResource == null || dcResource.isNew()){  // still doesn't exist in DC
            logger.debug("It doesn't exist in DC Doing a datacore.saveResource() with : {},{}", dcOrgProjectName.trim(),dcOrganization);
            DCResult newCreatedDCRes =  datacore.saveResource(dcOrgProjectName.trim(), toNewDCResource(dcOrganization));
            if(newCreatedDCRes.getResource() != null){
                logger.debug("Setting the new DC URI into the dcOrganization : {}", newCreatedDCRes.getResource().getUri() );
                dcOrganization.setId(newCreatedDCRes.getResource().getUri());
                return newCreatedDCRes.getResource();
            } 
        }
        
        // if version has changed : "Sorry, did change while you were editing it, please copy your fields, close and restart the wizard"
        return null;
    }

    /** Update DC Organization data re-using this.create(DCOrganization) method. */
    public DCResource update(DCOrganization dcOrganization){
        return this.create(dcOrganization);
    }

    /** Change rights of DC Organization. */
    public boolean changeDCOrganizationRights(DCResource dcResource,String kOrgId){
        final List<String> newRights = new ImmutableList.Builder<String>().add(kOrgId).build();
        final List<String> dcResultErrOutter = new ArrayList<String>();

        //get admin authentication and change organization rights 
        portalSystemUserService.runAs(
            new Runnable() {
                //Inner class with Runnable, its used as a function parameter (executed at parameter declaration)
                @Override
                public void run() {
                    DCRights dcRights = datacore.getRightsOnResource(dcOrgProjectName, dcResource).getRights();
                    dcRights.addOwners(newRights);
                    List<String> dcResultInner = datacore.setRightsOnResource(dcOrgProjectName, dcResource, dcRights).getErrorMessages();
                    dcResultErrOutter.addAll(dcResultInner);
                    if(dcResultInner != null && !dcResultInner.isEmpty()){
                        logger.error("There was an error while updating rights [{}]to resource : {}", dcRights, dcResource);
                    }
                }}
        );
        return dcResultErrOutter.isEmpty();
    }


    // Helper & Handler methods

    private DCResource mergeDCOrgToDCResources(DCOrganization fromOrg, DCResource toRes){
        // Organization data
        toRes.setMappedList("org:legalName", valueAsDCList(fromOrg.getLegal_name(), fromOrg.getLang())); //list
        toRes.setMappedList("odisp:name", valueAsDCList(
                fromOrg.getLegal_name()+","+fromOrg.getCity()+" - "+fromOrg.getCountry()
                , fromOrg.getLang())); //list - also find as org:displayName

        DCOrganizationType dcOrganizationType = DCOrganizationType.getDCOrganizationType(fromOrg.getSector_type());
        toRes.set("org:sector", (dcOrganizationType != null) ? dcOrganizationType.name() : "");

        toRes.set("org:status", fromOrg.isIn_activity() ? "Normal Activity" : "");
        toRes.setMappedList("org:altName", valueAsDCList(fromOrg.getAlt_name(), fromOrg.getLang())); //list
        toRes.set("org:type", fromOrg.getOrg_type());
        toRes.set("org:regNumber", fromOrg.getTax_reg_num());
        toRes.set("orgpu:officialId", fromOrg.getTax_reg_official_id()); /* Only for Public organizations*/
        toRes.set("org:activity", fromOrg.getTax_reg_activity_uri());
        if(dcOrganizationType.equals(DCOrganizationType.Public)){
            toRes.set("orgpu:jurisdiction", fromOrg.getJurisdiction_uri()); /* Only for Public organizations*/
        }
        toRes.set("org:phoneNumber", fromOrg.getPhone_number());
        toRes.set("org:webSite", fromOrg.getWeb_site());
        toRes.set("org:email", fromOrg.getEmail());
        // Geolocation data
        toRes.set("adrpost:streetAndNumber", fromOrg.getStreet_and_number());
        toRes.set("adrpost:supField", fromOrg.getAdditional_address_field());
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
    private List<Map<String, DCResource.Value>> valueAsDCList(String value, String language){
        Map<String, DCResource.Value> myMap = new HashMap<>(2);
        myMap.put("@language", new DCResource.StringValue(language));
        myMap.put("@value", new DCResource.StringValue(value));

        List<Map<String, DCResource.Value>> legalNameLst = new ArrayList<>(1);
        legalNameLst.add(myMap);
        return legalNameLst;
    }
    private DCResource toNewDCResource(DCOrganization dcOrganization){
        DCResource dcResource = new DCResource();
        mergeDCOrgToDCResources(dcOrganization, dcResource);

        dcResource = setDCIdOrganization(dcResource, dcOrganization.getSector_type(), dcOrganization.getTax_reg_num(), dcOrganization.getCountry_uri());

        return dcResource;
    }

    public  DCResource setDCIdOrganization(DCResource dcResource, String type, String regNumber, String country_uri){
        //"@id" : "http://data.ozwillo.com/dc/type/orgprfr:OrgPriv%C3%A9e_0/FR/47952557800049",
        String countryAcronym = getCountryAcronym(country_uri);
        String cx = countryAcronym.toLowerCase() == "en" ? "" : countryAcronym.toLowerCase(); //if english leave it to match with orgpr/orgpu
        String orgModelType = generateResourceType(type, country_uri, regNumber);

        dcResource.setBaseUri(dcBaseUri.trim());
        dcResource.setType(orgModelType);
        dcResource.setIri(cx.toUpperCase()+"/"+regNumber);
        return dcResource;
    }

    public String generateResourceType(String type, String country_uri, String regNumber){
        String px = DCOrganizationType.getDCOrganizationType(type).equals(DCOrganizationType.Private) ? "pr": "pu";
        //get country acronym
        String countryAcronym = getCountryAcronym(country_uri);
        String cx = countryAcronym .toLowerCase().equals("en") ? "" : countryAcronym .toLowerCase(); //if english leave it to match with orgpr/orgpu

        String orgModelPrefix = "org"+px+cx;
        String orgModelSuffix = dcOrgPrefixToSuffix.get(orgModelPrefix);
        String orgModelType = orgModelPrefix + ":" + orgModelSuffix + "_0";
        return orgModelType ;
    }

    public String getCountryAcronym(String country_uri){
        return (country_uri != null && !country_uri.isEmpty() ? country_uri.substring(country_uri.length() - 2) : "en");
    }

    private static final Map<String, String> dcOrgPrefixToSuffix = new ImmutableMap.Builder<String, String>()
            //private
            .put("orgpr",   "PrivateOrg")
            .put("orgprfr", "OrgPrivée")
            .put("orgprbg", "ЧастнаОрг")
            .put("orgprit", "OrgPrivata")
            .put("orgprtr", "ÖzelSektKuru")
            .put("orgpres", "OrgPrivada")
            //public
            .put("orgpu",   "PublicOrg")
            .put("orgpufr", "OrgPublique")
            .put("orgpubg", "ПубличнаОрг")
            .put("orgpuit", "OrgPubblica")
            .put("orgputr", "KamuKurumu")
            .put("orgpues", "OrgPública")
            .build();
    
    private static final List<String> dcOrgStatus = new ImmutableList.Builder<String>()
            //.add("Normal Activity")
            .add("Insolvent")
            .add("Bankrupt")
            .add("In Receivership")
            .build();
    
    public DCOrganization toDCOrganization(DCResource res, String language) {

        String legalName =       getBestI18nValue(res, language, "org:legalName", null); //Mapped list

        String in_activity_val = getBestI18nValue(res, language, "org:status", null);
        boolean in_activity =    (in_activity_val!= null && dcOrgStatus.contains(in_activity_val) ) ? true : false;

        String sector =          getBestI18nValue(res, language, "org:sector", null);
        String altName =         getBestI18nValue(res, language, "org:altName", null); //Mapped list

        String taxRegAct_uri =    getBestI18nValue(res, language, "org:activity", null);
        String taxRegAct =       (taxRegAct_uri == null) ? null : getBestI18nValue(
                                       datacore.getResourceFromURI(dcOrgProjectName, taxRegAct_uri).getResource(), language, "orgact:code", null
                                 );
        String officialId =      getBestI18nValue(res, language, "orgpu:officialId", null);
        String regNumber =       getBestI18nValue(res, language, "org:regNumber", null);

        String jurisdiction_uri =  getBestI18nValue(res, language, "orgpu:jurisdiction", null);
        String jurisdiction =    jurisdiction_uri == null ? null : getBestI18nValue(
                                       datacore.getResourceFromURI(dcOrgProjectName, jurisdiction_uri).getResource(), language, "geoci:displayName", null
                                 );

        String phoneNumber =     getBestI18nValue(res, language, "org:phoneNumber", null);
        String webSite =         getBestI18nValue(res, language, "org:webSite", null);
        String email =           getBestI18nValue(res, language, "org:email", null);
        // Geolocation data
        String streetAndNumber = getBestI18nValue(res, language, "adrpost:streetAndNumber", null);
        String supField =        getBestI18nValue(res, language, "adrpost:supField", null);
        String POBox =           getBestI18nValue(res, language, "adrpost:POBox", null);
        String city_uri =        getBestI18nValue(res, language, "adrpost:postName", null);
        String city =            city_uri == null ? null : getBestI18nValue(
                                        datacore.getResourceFromURI(dcOrgProjectName, city_uri).getResource(), language, "geoci:displayName", null
                                 );
        String zip =             getBestI18nValue(res, language, "adrpost:postCode", "org:postCode");
        String cedex =           getBestI18nValue(res, language, "adrpost:cedex", null);

        String country_uri =     getBestI18nValue(res, language, "adrpost:country", null);
        String country =         country_uri == null ? null : getBestI18nValue(
                                       datacore.getResourceFromURI(dcOrgProjectName, country_uri).getResource(), language, "geoco:name", null
                                 );

        //String longitude=     getBestI18nValue(res, "org:longitude", null);
        //String latitude =     getBestI18nValue(res, "org:latitude", null);

        DCOrganization dcOrg = new DCOrganization();
        dcOrg.setLegal_name(legalName);
        dcOrg.setIn_activity(in_activity);
        dcOrg.setSector_type(sector);
        dcOrg.setAlt_name(altName);

        dcOrg.setTax_reg_activity_uri(taxRegAct_uri); dcOrg.setTax_reg_activity(taxRegAct);
        dcOrg.setTax_reg_num(regNumber);
        dcOrg.setTax_reg_official_id(officialId); /* Only for public organizations*/

        dcOrg.setJurisdiction_uri(jurisdiction_uri); /* Only for public organizations*/
        dcOrg.setJurisdiction(jurisdiction); /* Only for public organizations*/

        dcOrg.setPhone_number(phoneNumber);
        dcOrg.setWeb_site(webSite);
        dcOrg.setEmail(email);
        
        dcOrg.setStreet_and_number(streetAndNumber);
        dcOrg.setAdditional_address_field(supField);
        dcOrg.setPo_box(POBox);
        dcOrg.setCedex(cedex);
        dcOrg.setCity_uri(city_uri);dcOrg.setCity(city);
        dcOrg.setZip(zip);
        dcOrg.setCountry_uri(country_uri); dcOrg.setCountry(country);

        dcOrg.setId(res.getUri());
        dcOrg.setExist(true); // Organization was found !
        dcOrg.setLang(language);
        dcOrg.setVersion(res.getVersion()+"");

        return dcOrg;
    }

    /**
     * Return the resource value, first matching the fieldName, if not found then match with the altFieldName.
     * In case a Listed Map is found, the inner values are matched using i18n key @language, and @value. 
     * @param resource
     * @param language
     * @param fieldName
     * @param altFieldName
     * @return String with found value, null if not value was found (empty counts as not found value)
     */
    @SuppressWarnings("unchecked")
    private String getBestI18nValue(DCResource resource, String language, String fieldName, String altFieldName){
        if(resource == null){return null;}
        Object object = resource.get(fieldName);
        if( object == null && (altFieldName != null && !altFieldName.isEmpty() ) ){
            logger.warn("Field \"{}\" not found. Fallback using field name \"{}\"", fieldName, altFieldName);
            object = resource.get(altFieldName);
        }
        if (object == null) { // if after double matched is not found then return null
            logger.warn("DC Resource {} of type {} has no field required fields.", resource.getUri(), resource.getType());
            return null;
        }
        // Parse the list checking if it's a simple list or a listed map
        if (object instanceof List ) {
            String valueMap = null;
            for (Object obj: (List<Object>) object) {
                if (obj instanceof Map) {
                    Map<String, String> nameMap = (Map<String, String>) obj;
                    logger.debug("nameMap: " + nameMap.toString());
                    String l = nameMap.get("@language"); // TODO Q why ?? @language only in application/json+ld, otherwise l
                    if (l == null) { continue; /* shouldn't happen */ }
                    if (l.equals(language)) {
                        String val = nameMap.get("@value");
                        return val == null || val.isEmpty() ? null : nameMap.get("@value"); //break; // can't find better
                    }
                    if (valueMap == null) { // takes the last valid match
                        valueMap = nameMap.get("@value"); // TODO Q why ?? @value only in application/json+ld, otherwise v
                    }
                }else {valueMap = ((List<String>)object).toString();} // Its a list of strings //TODO use it and test it
            }
            return valueMap;

        }else if (object instanceof String ) {
            String val = (String)object;
            return val == null || val.isEmpty() ? null : val;
        }
        return null;
    }


}
