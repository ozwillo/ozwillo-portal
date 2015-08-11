package org.oasis_eu.portal.services.dc.organization;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.model.network.UIOrganization;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.DCOrganizationType;
import org.oasis_eu.spring.kernel.model.OrganizationType;
import org.oasis_eu.spring.kernel.model.UserAccount;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserAccountService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class OrganizationService {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @Autowired
    private DCOrganizationService organizationDAO;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private HttpServletRequest request;

    @Value("${application.dcOrg.baseUri: http://data.ozwillo.com/dc/type}")
    private String dcBaseUri;

    /** Search an organization in DC and Kernel to validate its modification */
    public DCOrganization findOrganization(String contact_name,String contact_lastName,String contact_email,
            String country, String country_uri,String sector, String legalName, String regNumber)
    {
        String localLang = RequestContextUtils.getLocale(request).getLanguage();
        String dcSectorType = DCOrganizationType.getDCOrganizationType(sector).name();
        DCOrganization dcOrganization = organizationDAO.searchOrganization(localLang, country_uri, dcSectorType, legalName, regNumber);

        if(dcOrganization == null || !dcOrganization.isExist()){ // Organization doesn't exist in DC
            logger.info("Organization doesn't exist in DC. Letting user create one with given entries.");
            // set an empty DCOrganization to be filled by user then Create Organization in Kernel when creating

            String type = organizationDAO.generateResourceType(dcSectorType, country_uri, regNumber);
            String baseUri = dcBaseUri.trim(); //"http://data.ozwillo.com/dc/type";
            String iri = organizationDAO.getCountryAcronym(country_uri).toUpperCase()+"/"+regNumber;
            UIOrganization uiOrganization = networkService.searchOrganizationByDCId(baseUri+"/"+type+"/"+iri);
            if(uiOrganization != null){
                logger.debug("It already exist in kernel, so cant be re-created.");
                return null; // there is an owner for this data, so it should show the message to "Ask a colleague to invite you" in front-end
            }

            dcOrganization = new DCOrganization();
            //contact data
            dcOrganization.setContact_name(contact_name);
            dcOrganization.setContact_lastName(contact_lastName);
            dcOrganization.setContact_email(contact_email);
            //organization data
            dcOrganization.setLegal_name(legalName);
            dcOrganization.setTax_reg_num(regNumber);
            dcOrganization.setSector_type(sector);
            dcOrganization.setEmail(contact_email); // Do it only in case of new organization
            dcOrganization.setZip("00000");
            dcOrganization.setCountry_uri(country_uri); dcOrganization.setCountry(country);
            return dcOrganization;

        }else {
            UIOrganization uiOrganization = networkService.searchOrganizationByDCId(dcOrganization.getId());
            if( uiOrganization == null){ // found in DC but not in KERNEL, so modification is allowed
                logger.info("Organization found in DC but not in KERNEL, so modification by the user is allowed.");
                // re-set contact data
                dcOrganization.setContact_name(contact_name);
                dcOrganization.setContact_lastName(contact_lastName);
                dcOrganization.setContact_email(contact_email);
                //Set the sector type supported by the UI
                dcOrganization.setSector_type(OrganizationType.getOrganizationType(dcOrganization.getSector_type()).name());
                dcOrganization.setCountry_uri(country_uri); dcOrganization.setCountry(country);
                return dcOrganization; // there is no owner for the data, so can be modified(in DC) & created (in kernel)
            }else{
                logger.debug("There is an owner for this data, so it should show a message to the user in front-end");
                return null; // there is an owner for this data, so it should show the message to "Ask a colleague to invite you" in front-end
            }
        }
    }

    public DCOrganization findOrganizationById(String dc_id){
        String encodedDCId = UriComponentsBuilder.fromUriString(dc_id).build().encode().toString();
        DCOrganization dcOrganization = organizationDAO.searchOrganizationById(encodedDCId);
        if(dcOrganization != null){
            dcOrganization.setSector_type(OrganizationType.getOrganizationType(dcOrganization.getSector_type()).name());
        }

        return dcOrganization;
    }

    /** Create organization in DC and create/update data in kernel */
    public UIOrganization create(DCOrganization dcOrganization) {
        if(dcOrganization.getLang() == null || dcOrganization.getLang().isEmpty()){dcOrganization.setLang(RequestContextUtils.getLocale(request).getLanguage());}
        // create DC Organization
        DCResource dcResource = organizationDAO.create(dcOrganization);
        updateUserInfo(dcOrganization);
        if(dcResource != null && !dcOrganization.isExist()){
            UIOrganization uiOrganization = checkAndCreateKernelOrganization(dcOrganization); 
            if(uiOrganization == null){ // if null, then the organization exists in kernel.
                logger.error("Kernel Organization had been created since you've started filling in the form.");
                throw new WrongQueryException();
            }
            logger.debug("The organization exists in kernel : " + uiOrganization.toString());
            return uiOrganization;
        }
        return null;
    }

    /** Update organization in DC and create data in kernel */
    public UIOrganization update(DCOrganization dcOrganization) {
        if(dcOrganization.getLang() == null || dcOrganization.getLang().isEmpty()){dcOrganization.setLang(RequestContextUtils.getLocale(request).getLanguage());}
        updateUserInfo(dcOrganization);
        if(Integer.parseInt(dcOrganization.getVersion()) >= 0){
            UIOrganization uiOrganization = checkAndCreateKernelOrganization(dcOrganization);
            if(uiOrganization != null){ // if null, then the organization exists in kernel.
                //If not null, the organization was created in Kernel, then update data rights in DC.
                DCResource dcResource = organizationDAO.setDCIdOrganization(new DCResource(), dcOrganization.getSector_type(), 
                        dcOrganization.getTax_reg_num(), dcOrganization.getCountry_uri());
                dcResource.setVersion(Integer.parseInt(dcOrganization.getVersion()));
                if(organizationDAO.changeDCOrganizationRights(dcResource,  uiOrganization.getId())){
                    //If rights have changed, then the version has increased in DC, so we increase it here as well.
                    dcOrganization.setVersion((Integer.parseInt(dcOrganization.getVersion())+1)+"");
                    // now it can (by right) update an DC Organization
                    organizationDAO.update(dcOrganization);
                }
            }else{ // Not updated
                logger.error("Kernel Organization had been created since you've started filling in the form.");
                throw new IllegalArgumentException();
            }
            return uiOrganization;
        }
        return null;
    }


    /** Check and create kernel organization */
    private UIOrganization checkAndCreateKernelOrganization(DCOrganization dcOrganization) {
        URI territoryId = null;
        URI dcId = null;

        //Translate Kernel SectorType (company/public_body) into DC SectorType (Public/Private)
        OrganizationType sectorType = OrganizationType.getOrganizationType(dcOrganization.getSector_type());

        try {
            if(sectorType.equals(OrganizationType.PUBLIC_BODY)){
                territoryId = new URI(dcOrganization.getJurisdiction_uri());
            }
            dcId = new URI(dcOrganization.getId());
        } catch (URISyntaxException e) {
            // Jurisdiction is not correct so can't be parsed as URI
            logger.error("The Jurisdiction \"{}\" or DCOrganization ID \"{}\" can't be parsed into URI. "
                    + "Verify that they are definied correctly.", dcOrganization.getJurisdiction_uri(), dcOrganization.getId());
            logger.error("Error : {}", e.getMessage());
            throw new IllegalArgumentException();
        }

        UIOrganization searchKOrganization = networkService.searchOrganizationByDCId(dcOrganization.getId());
        if(searchKOrganization == null){ // org not found in kernel
            UIOrganization creqtedKOrg = networkService.createOrganization(dcOrganization.getLegal_name(),sectorType.name(),territoryId,dcId);
            if(creqtedKOrg != null && creqtedKOrg.getId() != null ){
                return creqtedKOrg;
            }
        }else{
            networkService.updateOrganization(searchKOrganization);
            return searchKOrganization; //this is to return a value so it can continue and update data in DC
        }

        return null;
    }

    private void updateUserInfo(DCOrganization dcOrganization) {
        UserInfo userInfo = userInfoService.currentUser();
        boolean isChangefound = false;
        
        String givenName = userInfo.getGivenName() != null ? userInfo.getGivenName() : "";
        String familyName = userInfo.getFamilyName()!= null ? userInfo.getFamilyName() : "";
        String email = userInfo.getEmail()!= null ? userInfo.getEmail() : "";
        
        //Only if has changes will update
        if(dcOrganization.getContact_name() != null && !givenName.equals(dcOrganization.getContact_name())){
            userInfo.setGivenName(dcOrganization.getContact_name()); isChangefound = true;
        }
        if(dcOrganization.getContact_lastName() != null && !familyName.equals(dcOrganization.getContact_lastName()) ){
            userInfo.setFamilyName(dcOrganization.getContact_lastName()); isChangefound = true;
        }
        if(dcOrganization.getContact_email() != null && !email.equals(dcOrganization.getContact_email()) ){
            userInfo.setEmail(dcOrganization.getContact_email()); isChangefound = true;
        }
        if (isChangefound){
            logger.info("Updating user information: {}", userInfo); //TODO LATTER add toString() to UserInfo
            userAccountService.saveUserAccount(new UserAccount(userInfo));
        }
    }

}
