package org.oasis_eu.portal.services.dc.organization;

import com.google.common.base.Strings;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.model.user.UserProfile;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.portal.ui.UIOrganization;
import org.oasis_eu.portal.services.kernel.UserProfileService;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.DCOrganizationType;
import org.oasis_eu.spring.kernel.model.OrganizationType;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Service
public class OrganizationService {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @Autowired
    private DCOrganizationService dcOrganizationService;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private ImageService imageService;

    @Value("${application.dcOrg.baseUri: http://data.ozwillo.com/dc/type}")
    private String dcBaseUri;
    @Value("${application.defaultIconUrl: /img/noicon.png")
    private String defaultIconUrl;


    /**
     * Search an organization in DC and Kernel to validate its creation / modification in the portal, and bootstrap
     * a new one if not found in DC and Kernel
     */
    public DCOrganization findOrBootstrapOrganization(String country, String countryUri, String sector, String legalName,
        String regNumber) {
        String localLang = RequestContextUtils.getLocale(request).getLanguage();
        String dcSectorType = DCOrganizationType.getDCOrganizationType(sector).name();
        DCOrganization dcOrganization = dcOrganizationService.searchOrganization(localLang, countryUri, dcSectorType, regNumber);
        if (dcOrganization == null) {
            // An organization who has changed of regNumber is not retrieved with a classical search
            // So try to load it directly instead
            Optional<DCOrganization> optOrg =
                dcOrganizationService.findOrganizationById(dcOrganizationService.generateDcId(countryUri, regNumber), localLang);
            if (optOrg.isPresent())
                dcOrganization = optOrg.get();
        }

        if (dcOrganization == null) { // Organization doesn't exist in DC
            logger.info("Organization doesn't exist in DC, letting user create one with given entries");
            // set an empty DCOrganization to be filled by user then Create Organization in Kernel when creating

            String dcId = dcOrganizationService.generateDcId(countryUri, regNumber);
            if (networkService.searchOrganizationByDCId(dcId) != null) {
                logger.warn("It already exists in kernel (it shouldn't as it does not exist in DC !), so cant be re-created.");
                return null; // there is an owner for this data, so it should show the message to "Ask a colleague to invite you" in front-end
            }

            dcOrganization = new DCOrganization();
            dcOrganization.setLegal_name(legalName);
            dcOrganization.setTax_reg_num(regNumber);
            dcOrganization.setSector_type(sector);
            dcOrganization.setCountry_uri(countryUri);
            dcOrganization.setCountry(country);
            return dcOrganization;

        } else {
            List<String> organizationAliases = dcOrganizationService.getOrganizationAliases(dcOrganization.getId());
            UIOrganization uiOrganization = networkService.searchOrganizationByDCIdAndAliases(organizationAliases);
            if (uiOrganization == null) { // found in DC but not in KERNEL, so modification is allowed
                logger.info("Organization found in DC but not in KERNEL, so modification by the user is allowed.");
                //Set the sector type supported by the UI
                dcOrganization.setSector_type(OrganizationType.getOrganizationType(dcOrganization.getSector_type()).name());
                dcOrganization.setCountry_uri(countryUri);
                dcOrganization.setCountry(country);
                return dcOrganization; // there is no owner for the data, so can be modified(in DC) & created (in kernel)
            } else {
                logger.debug("There is an owner for this data, so it should show a message to the user in front-end");
                return null; // there is an owner for this data, so it should show the message to "Ask a colleague to invite you" in front-end
            }
        }
    }

    public List<DCOrganization> findOrganizations(String country_uri, String query) {
        String lang = RequestContextUtils.getLocale(request).getLanguage();
        List<DCOrganization> organizations =
            dcOrganizationService.searchOrganizations(lang, country_uri, query);
        // transform DC representation of sector to UI ones before returning organizations
        // otherwise, public sector organizations won't be correctly managed during creation
        organizations.forEach(dcOrganization ->
            dcOrganization.setSector_type(OrganizationType.getOrganizationType(dcOrganization.getSector_type()).name()));
        return organizations;
    }

    public DCOrganization getOrganization(String dcId) {
        Optional<DCOrganization> optionalDcOrganization =
            dcOrganizationService.findOrganizationById(dcId, RequestContextUtils.getLocale(request).getLanguage());
        if (optionalDcOrganization.isPresent()) {
            DCOrganization dcOrganization = optionalDcOrganization.get();
            dcOrganization.setSector_type(OrganizationType.getOrganizationType(optionalDcOrganization.get().getSector_type()).name());

            //load icon stored in local DB
            String iconUrl = imageService.buildObjectIconImageVirtualUrlOrNullIfNone(dcOrganization.getTax_reg_num());
            if (iconUrl == null) {
                iconUrl = defaultIconUrl.trim();
            }
            dcOrganization.setIconUrl(iconUrl);

            return dcOrganization;
        }

        return null;
    }

    public boolean existsOrganizationOrAliasesInKernel(String dcId) {
        List<String> orgAliases = dcOrganizationService.getOrganizationAliases(dcId);
        logger.debug("Got organization aliases : {}", orgAliases);
        return orgAliases.stream().anyMatch(orgAlias -> networkService.searchOrganizationByDCId(orgAlias) != null);
    }

    /**
     * Create organization in DC and create/update data in kernel
     */
    public UIOrganization create(DCOrganization dcOrganization, boolean updateUserInfo) {
        if (dcOrganization.getLang() == null || dcOrganization.getLang().isEmpty()) {
            dcOrganization.setLang(RequestContextUtils.getLocale(request).getLanguage());
        }
        // create DC Organization
        DCResource dcResource = dcOrganizationService.create(dcOrganization);
        if (updateUserInfo) updateUserInfo(dcOrganization);
        if (dcResource != null && !dcOrganization.isExist()) {
            UIOrganization uiOrganization = createOrUpdateKernelOrganization(dcOrganization);
            if (uiOrganization == null) { // if null, then the organization exists in kernel.
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

    /**
     * Update organization in DC and create data in kernel
     */
    public UIOrganization update(DCOrganization dcOrganization) {
        if (Strings.isNullOrEmpty(dcOrganization.getLang())) {
            dcOrganization.setLang(RequestContextUtils.getLocale(request).getLanguage());
        }
        updateUserInfo(dcOrganization);

        if (Integer.parseInt(dcOrganization.getVersion()) >= 0) {

            // If organization id has changed, update first in DC
            String newDcOrganizationId = dcOrganizationService.generateDcId(dcOrganization.getCountry_uri(), dcOrganization.getTax_reg_num());
            if (!newDcOrganizationId.equals(dcOrganization.getId())) {
                logger.debug("Changing organization id to {}", newDcOrganizationId);
                dcOrganization = dcOrganizationService.toDCOrganization(dcOrganizationService.update(dcOrganization), dcOrganization.getLang());
            }

            UIOrganization uiOrganization = createOrUpdateKernelOrganization(dcOrganization);

            if (uiOrganization != null) { // the organization was created or updated in Kernel, then update data rights in DC.
                DCResource dcResource = dcOrganizationService.setDCIdOrganization(new DCResource(),
                    dcOrganization.getTax_reg_num(), dcOrganization.getCountry_uri());
                dcResource.setVersion(Integer.parseInt(dcOrganization.getVersion()));

                //if (dcOrganizationService.changeDCOrganizationRights(dcResource, uiOrganization.getId())) {
                    //If rights have changed, then the version has increased in DC, so we increase it here as well.
                    //dcOrganization.setVersion(String.valueOf(Integer.parseInt(dcOrganization.getVersion()) + 1));
                    // now it can (by right) update an DC Organization
                    dcOrganizationService.update(dcOrganization);
                //}
            } else { // Not updated
                String message = String.format("Kernel Organization (%s) had been edited since you've started filling in the form.",
                    dcOrganization.getAlt_name());
                logger.error(message);
                throw new IllegalArgumentException(message);
            }
            return uiOrganization;
        }
        return null;
    }

    /**
     * Check and create kernel organization.
     *
     * @return the created / modified organization or null if failed to create / update
     */
    private UIOrganization createOrUpdateKernelOrganization(DCOrganization dcOrganization) {
        URI territoryId = null;
        URI dcId;

        //Translate Kernel SectorType (company/public_body) into DC SectorType (Public/Private)
        OrganizationType sectorType = OrganizationType.getOrganizationType(dcOrganization.getSector_type());

        try {
            if (sectorType == OrganizationType.PUBLIC_BODY) {
                territoryId = new URI(dcOrganization.getJurisdiction_uri());
            }
            dcId = new URI(dcOrganization.getId());
        } catch (URISyntaxException e) {
            logger.error("The Jurisdiction \"{}\" or DCOrganization ID \"{}\" can't be parsed into URI. "
                + "Verify that they are defined correctly.", dcOrganization.getJurisdiction_uri(), dcOrganization.getId());
            logger.error("Error : {}", e.getMessage());
            throw new IllegalArgumentException(e);
        }

        List<String> organizationAliases = dcOrganizationService.getOrganizationAliases(dcOrganization.getId());
        UIOrganization knOrganization = networkService.searchOrganizationByDCIdAndAliases(organizationAliases);

        if (knOrganization == null) {
            // org not found in kernel
            UIOrganization createdKOrg =
                    networkService.createOrganization(dcOrganization.getLegal_name(), sectorType.name(), territoryId, dcId);
            if (createdKOrg != null && createdKOrg.getId() != null) {
                return createdKOrg;
            }
        } else {
            knOrganization.setName(dcOrganization.getLegal_name());
            knOrganization.setType(sectorType);
            knOrganization.setTerritoryId(territoryId);
            knOrganization.setDcId(dcId);
            networkService.updateOrganizationInfo(knOrganization);
            return knOrganization; //this is to return a value so it can continue and update data in DC
        }

        return null;
    }

    private void updateUserInfo(DCOrganization dcOrganization) {
        UserProfile userProfile = userProfileService.findUserProfile(userInfoService.currentUser().getUserId());
        boolean isChangefound = false;

        String givenName = userProfile.getGivenName() != null ? userProfile.getGivenName() : "";
        String familyName = userProfile.getFamilyName() != null ? userProfile.getFamilyName() : "";

        //Only if has changes will update
        if (dcOrganization.getContact_name() != null && !givenName.equals(dcOrganization.getContact_name())) {
            userProfile.setGivenName(dcOrganization.getContact_name());
            isChangefound = true;
        }
        if (dcOrganization.getContact_lastName() != null && !familyName.equals(dcOrganization.getContact_lastName())) {
            userProfile.setFamilyName(dcOrganization.getContact_lastName());
            isChangefound = true;
        }
        if (isChangefound) {
            logger.info("Updating user information: {}", userProfile); //TODO LATTER add toString() to UserInfo
            userProfileService.saveUserProfile(userProfile);
        }
    }
}
