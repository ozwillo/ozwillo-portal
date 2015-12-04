package org.oasis_eu.portal.services.dc.organization;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Strings;
import org.oasis_eu.portal.core.services.icons.ImageService;
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
	@Autowired
	private ImageService imageService;

	@Value("${application.dcOrg.baseUri: http://data.ozwillo.com/dc/type}")
	private String dcBaseUri;
	@Value("${application.defaultIconUrl: /img/noicon.png")
	private String defaultIconUrl;


	/** Search an organization in DC and Kernel to validate its modification */
	public DCOrganization findOrganization(String country, String countryUri, String sector, String legalName, String regNumber)
	{
		String localLang = RequestContextUtils.getLocale(request).getLanguage();
		String dcSectorType = DCOrganizationType.getDCOrganizationType(sector).name();
		DCOrganization dcOrganization = organizationDAO.searchOrganization(localLang, countryUri, dcSectorType, legalName, regNumber);

		if(dcOrganization == null || !dcOrganization.isExist()){ // Organization doesn't exist in DC
			logger.info("Organization doesn't exist in DC. Letting user create one with given entries.");
			// set an empty DCOrganization to be filled by user then Create Organization in Kernel when creating

			String type = organizationDAO.generateResourceType(dcSectorType, countryUri, regNumber);
			String baseUri = dcBaseUri.trim(); //"http://data.ozwillo.com/dc/type";
			String iri = organizationDAO.getCountryAcronym(countryUri).toUpperCase()+ '/' +regNumber;
			UIOrganization uiOrganization = networkService.searchOrganizationByDCId(baseUri+ '/' +type+ '/' +iri);
			if(uiOrganization != null){
				logger.debug("It already exist in kernel, so cant be re-created.");
				return null; // there is an owner for this data, so it should show the message to "Ask a colleague to invite you" in front-end
			}

			dcOrganization = new DCOrganization();
			dcOrganization.setLegal_name(legalName);
			dcOrganization.setTax_reg_num(regNumber);
			dcOrganization.setSector_type(sector);
			dcOrganization.setCountry_uri(countryUri);
			dcOrganization.setCountry(country);
			return dcOrganization;

		}else {
			UIOrganization uiOrganization = networkService.searchOrganizationByDCId(dcOrganization.getId());
			if( uiOrganization == null){ // found in DC but not in KERNEL, so modification is allowed
				logger.info("Organization found in DC but not in KERNEL, so modification by the user is allowed.");
				//Set the sector type supported by the UI
				dcOrganization.setSector_type(OrganizationType.getOrganizationType(dcOrganization.getSector_type()).name());
				dcOrganization.setCountry_uri(countryUri);
				dcOrganization.setCountry(country);
				return dcOrganization; // there is no owner for the data, so can be modified(in DC) & created (in kernel)
			}else{
				logger.debug("There is an owner for this data, so it should show a message to the user in front-end");
				return null; // there is an owner for this data, so it should show the message to "Ask a colleague to invite you" in front-end
			}
		}
	}

	public DCOrganization findOrganizationById(String dcId){
		DCOrganization dcOrganization = organizationDAO.searchOrganizationById(dcId, RequestContextUtils.getLocale(request).getLanguage());
		if(dcOrganization != null){
			dcOrganization.setSector_type(OrganizationType.getOrganizationType(dcOrganization.getSector_type()).name());

			//load icon stored in local DB
			String iconUrl = imageService.buildObjectIconImageVirtualUrlOrNullIfNone(dcOrganization.getTax_reg_num());
			if (iconUrl == null) {
				iconUrl = defaultIconUrl.trim();
			}
			dcOrganization.setIconUrl(iconUrl);
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
			UIOrganization uiOrganization = createOrUpdateKernelOrganization(dcOrganization);
			if(uiOrganization == null){ // if null, then the organization exists in kernel.
				String message = String.format("Kernel Organization (%s) had been created since you've started filling in the form.",
						dcOrganization.getAlt_name());
				logger.error(message);
				throw new WrongQueryException(message);
			}
			logger.debug("The organization exists in kernel : " + uiOrganization);
			return uiOrganization;
		}
		return null;
	}

	/** Update organization in DC and create data in kernel */
	public UIOrganization update(DCOrganization dcOrganization) {
		if(Strings.isNullOrEmpty(dcOrganization.getLang())){
			dcOrganization.setLang(RequestContextUtils.getLocale(request).getLanguage());
		}
		updateUserInfo(dcOrganization);

		if(Integer.parseInt(dcOrganization.getVersion()) >= 0){
			UIOrganization uiOrganization = createOrUpdateKernelOrganization(dcOrganization);

			if(uiOrganization != null){ // the organization was created or updated in Kernel, then update data rights in DC.
				DCResource dcResource = organizationDAO.setDCIdOrganization(new DCResource(), dcOrganization.getSector_type(),
						dcOrganization.getTax_reg_num(), dcOrganization.getCountry_uri());
				dcResource.setVersion(Integer.parseInt(dcOrganization.getVersion()));

				if(organizationDAO.changeDCOrganizationRights(dcResource,  uiOrganization.getId())){
					//If rights have changed, then the version has increased in DC, so we increase it here as well.
					dcOrganization.setVersion(String.valueOf(Integer.parseInt(dcOrganization.getVersion()) + 1));
					// now it can (by right) update an DC Organization
					organizationDAO.update(dcOrganization);
				}
			}else{ // Not updated
				String message = String.format("Kernel Organization (%s) had been edited since you've started filling in the form.",
						dcOrganization.getAlt_name());
				logger.error(message);
				throw new IllegalArgumentException(message);
			}
			return uiOrganization;
		}
		return null;
	}

	/** Check and create kernel organization */
	private UIOrganization createOrUpdateKernelOrganization(DCOrganization dcOrganization) {
		URI territoryId = null;
		URI dcId;

		//Translate Kernel SectorType (company/public_body) into DC SectorType (Public/Private)
		OrganizationType sectorType = OrganizationType.getOrganizationType(dcOrganization.getSector_type());

		try {
			if(sectorType == OrganizationType.PUBLIC_BODY){
				territoryId = new URI(dcOrganization.getJurisdiction_uri());
			}
			dcId = new URI(dcOrganization.getId());
		} catch (URISyntaxException e) {
			// Jurisdiction is not correct so can't be parsed as URI
			logger.error("The Jurisdiction \"{}\" or DCOrganization ID \"{}\" can't be parsed into URI. "
					+ "Verify that they are definied correctly.", dcOrganization.getJurisdiction_uri(), dcOrganization.getId());
			logger.error("Error : {}", e.getMessage());
			throw new IllegalArgumentException(e);
		}

		UIOrganization searchKOrganization = networkService.searchOrganizationByDCId(dcOrganization.getId());
		if(searchKOrganization == null){ // org not found in kernel
			UIOrganization createdKOrg = networkService.createOrganization(dcOrganization.getLegal_name(),sectorType.name(),territoryId,dcId);
			if(createdKOrg != null && createdKOrg.getId() != null ){
				return createdKOrg;
			}
		}
		if (searchKOrganization.isAdmin()){
			// TODO LATER fillUiOrgFromDcOrg(searchKOrganization, dcOrganization) ?
			// NB. actually ONLY territory_id can change (not legal name nor type, so it should be the same)
			searchKOrganization.setName(dcOrganization.getLegal_name());
			searchKOrganization.setType(sectorType);
			searchKOrganization.setTerritoryId(territoryId);
			// update existing org in Kernel
			networkService.updateOrganizationInfo(searchKOrganization);
		}else{
			logger.debug("Not admin of organisation {}, so can't update it in Kernel.",searchKOrganization.getName());
		}
		return searchKOrganization; //this is to return a value so it can continue and update data in DC
	}

	private void updateUserInfo(DCOrganization dcOrganization) {
		UserInfo userInfo = userInfoService.currentUser();
		boolean isChangefound = false;

		String givenName = userInfo.getGivenName() != null ? userInfo.getGivenName() : "";
		String familyName = userInfo.getFamilyName()!= null ? userInfo.getFamilyName() : "";
		String email = userInfo.getEmail()!= null ? userInfo.getEmail() : "";

		//Only if has changes will update
		if(dcOrganization.getContact_name() != null && !givenName.equals(dcOrganization.getContact_name())){
			userInfo.setGivenName(dcOrganization.getContact_name());
			isChangefound = true;
		}
		if(dcOrganization.getContact_lastName() != null && !familyName.equals(dcOrganization.getContact_lastName()) ){
			userInfo.setFamilyName(dcOrganization.getContact_lastName());
			isChangefound = true;
		}
		if(dcOrganization.getContact_email() != null && !email.equals(dcOrganization.getContact_email()) ){
			userInfo.setEmail(dcOrganization.getContact_email());
			isChangefound = true;
		}
		if (isChangefound){
			logger.info("Updating user information: {}", userInfo); //TODO LATTER add toString() to UserInfo
			userAccountService.saveUserAccount(new UserAccount(userInfo));
		}
	}
}
