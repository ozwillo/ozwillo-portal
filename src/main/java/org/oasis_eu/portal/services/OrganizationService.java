package org.oasis_eu.portal.services;

import org.oasis_eu.portal.model.dc.DCOrganization;
import org.oasis_eu.portal.model.instance.MyAppsInstance;
import org.oasis_eu.portal.model.kernel.instance.ApplicationInstance;
import org.oasis_eu.portal.model.kernel.organization.OrgMembership;
import org.oasis_eu.portal.model.kernel.organization.PendingOrgMembership;
import org.oasis_eu.portal.model.kernel.organization.UserMembership;
import org.oasis_eu.portal.model.organization.InvitationRequest;
import org.oasis_eu.portal.model.organization.UIOrganization;
import org.oasis_eu.portal.model.organization.UIOrganizationMember;
import org.oasis_eu.portal.model.organization.UIPendingOrganizationMember;
import org.oasis_eu.portal.model.user.UserGeneralInfo;
import org.oasis_eu.portal.services.dc.DCOrganizationService;
import org.oasis_eu.portal.services.kernel.UserMembershipService;
import org.oasis_eu.portal.services.kernel.UserProfileService;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.oasis_eu.spring.kernel.exception.EntityNotFoundException;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.*;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.support.RequestContextUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

@Service
public class OrganizationService {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserMembershipService userMembershipService;

    @Autowired
    private OrganizationStore organizationStore;

    @Autowired
    private ApplicationService applicationService;


    @Autowired
    private DCOrganizationService dcOrganizationService;

    @Value("${application.dcOrg.baseUri: http://data.ozwillo.com/dc/type}")
    private String dcBaseUri;

    @Value("${application.defaultIconUrl: /img/noicon.png")
    private String defaultIconUrl;

    @Value("${application.organizationDaysTillDeletedFromTrash:7}")
    private int organizationDaysTillDeletedFromTrash;

    /**
     * Checks whether the given organization is already registered in the kernel.
     */
    public boolean existsInKernel(String countryUri, String taxRegNumber) {
        String dcId = dcOrganizationService.generateDcId(countryUri, taxRegNumber);
        List<String> organizationAliases = dcOrganizationService.getOrganizationAliases(dcId);
        UIOrganization uiOrganization = searchOrganizationByDCIdAndAliases(organizationAliases);
        return uiOrganization != null;
    }

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
            if (searchOrganizationByDCId(dcId) != null) {
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
            UIOrganization uiOrganization = searchOrganizationByDCIdAndAliases(organizationAliases);
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
            return dcOrganization;
        }

        return null;
    }

    public List<UserMembership> searchUserMembershipsFromQuery(String query){
        String userId = userInfoService.currentUser().getUserId();
        List<UserMembership> userMemberships = new ArrayList<>(userMembershipService.getMembershipsOfUser(userId));
        //create a personal organization
        UserMembership personalMembership = new UserMembership();
        personalMembership.setId(userId);
        personalMembership.setAdmin(true);
        personalMembership.setOrganizationId(userId);
        personalMembership.setOrganizationName("Personal");
        userMemberships.add(personalMembership);

        //permit to compare string from different language (cf : question 32117953 on stackoverflow)
        Pattern p = Pattern.compile(query, Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        return userMemberships
                .stream()
                .filter(userMembership -> p.matcher(userMembership.getOrganizationName()).find())
                .collect(Collectors.toList());
    }

    public boolean existsOrganizationOrAliasesInKernel(String dcId) {
        List<String> orgAliases = dcOrganizationService.getOrganizationAliases(dcId);
        logger.debug("Got organization aliases : {}", orgAliases);
        return orgAliases.stream().anyMatch(orgAlias -> searchOrganizationByDCId(orgAlias) != null);
    }

    /**
     * Create organization in DC and create/update data in kernel
     */
    public UIOrganization create(DCOrganization dcOrganization) {
        if (dcOrganization.getLang() == null || dcOrganization.getLang().isEmpty()) {
            dcOrganization.setLang(RequestContextUtils.getLocale(request).getLanguage());
        }
        dcOrganization.setId(dcOrganizationService.generateDcId(dcOrganization.getCountry_uri(), dcOrganization.getTax_reg_num()));
        dcOrganization.setExist(false);

        DCResource dcResource = dcOrganizationService.createOrUpdate(dcOrganization);
        return createOrUpdateKernelOrganization(dcOrganization);
    }

    /**
     * Update organization in DC and create data in kernel
     */
    public UIOrganization update(DCOrganization dcOrganization) {
        if (StringUtils.isEmpty(dcOrganization.getLang())) {
            dcOrganization.setLang(RequestContextUtils.getLocale(request).getLanguage());
        }

        if (Integer.parseInt(dcOrganization.getVersion()) >= 0) {

            // If organization id has changed, update first in DC
            String newDcOrganizationId = dcOrganizationService.generateDcId(dcOrganization.getCountry_uri(), dcOrganization.getTax_reg_num());
            if (!newDcOrganizationId.equals(dcOrganization.getId())) {
                logger.debug("Changing organization id to {}", newDcOrganizationId);
                dcOrganization = dcOrganizationService.toDCOrganization(dcOrganizationService.update(dcOrganization), dcOrganization.getLang());
            }

            UIOrganization uiOrganization = createOrUpdateKernelOrganization(dcOrganization);

            dcOrganizationService.update(dcOrganization);

            // FIXME : quite inefficient to reload the organization with its instances and members, optimize it later
            return getOrganizationFromKernel(uiOrganization.getId());
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
        UIOrganization knOrganization = searchOrganizationByDCIdAndAliases(organizationAliases);

        if (knOrganization == null) {
            // org not found in kernel
            return createOrganization(dcOrganization.getLegal_name(), sectorType.name(), territoryId, dcId);
        } else {
            knOrganization.setName(dcOrganization.getLegal_name());
            knOrganization.setType(sectorType);
            knOrganization.setTerritoryId(territoryId);
            knOrganization.setDcId(dcId);
            updateOrganizationInfo(knOrganization);
            return knOrganization; //this is to return a value so it can continue and update data in DC
        }
    }

    private UIOrganization searchOrganizationByDCIdAndAliases(List<String> dcIds) {
        // Search for existing organization having "GET /d/org?dc_id=xx"
        Organization organization = null;
        for (String dcId : dcIds) {
            organization = organizationStore.findByDCID(dcId);
            if (organization != null)
                break;
        }

        return organization != null ?
                UIOrganization.fromKernelOrganization(organization, computeDeletionPlanned(organization), getUserName(organization.getStatusChangeRequesterId())) : null;
    }

    private UIOrganization searchOrganizationByDCId(String dcIc) {
        // Search for existing organization having "GET /d/org?dc_id=xx"
        Organization org = organizationStore.findByDCID(dcIc);
        return org != null ? UIOrganization.fromKernelOrganization(org, computeDeletionPlanned(org), getUserName(org.getStatusChangeRequesterId())) : null;
    }

    private UIOrganization createOrganization(String name, String type, URI territoryId, URI dcId) {
        logger.info("Request to create an organization: {} of type {} from user {} ({})", name, type,
                userInfoService.currentUser() != null ? userInfoService.currentUser().getUserId() : "SystemAdminUser",
                userInfoService.currentUser() != null ? userInfoService.currentUser().getEmail() : "no_email");

        //NB. If territory(jurisdiction) is an optional field (is set when sector type is public, so then it will be provided)...
        if (type == null || dcId == null /*&&territoryId==null*/) {
            throw new IllegalArgumentException();
        }

        Organization org = new Organization();
        org.setName(name);
        org.setType(OrganizationType.valueOf(type));
        org.setTerritoryId(territoryId);
        org.setDcId(dcId);

        org = organizationStore.create(org);

        UIOrganization result = UIOrganization.fromKernelOrganization(org, computeDeletionPlanned(org), getUserName(org.getStatusChangeRequesterId()));
        result.setAdmin(true);

        return result;
    }

    public List<UIOrganization> getMyOrganizations() {
        List<UIOrganization> organizations = new ArrayList<>();
        String userId = userInfoService.currentUser().getUserId();

        List<UserMembership> userMemberships = userMembershipService.getMembershipsOfUser(userId);
        for (UserMembership u : userMemberships) {
            Organization org = organizationStore.find(u.getOrganizationId());
            UIOrganization uiOrg = UIOrganization.fromKernelOrganization(org, computeDeletionPlanned(org), getUserName(org.getStatusChangeRequesterId()));
            uiOrg.setAdmin(u.isAdmin());
            organizations.add(uiOrg);
        }

        UIOrganization uiOrganization = new UIOrganization();
        uiOrganization.setId(userId);
        uiOrganization.setName("Personal");
        uiOrganization.setPersonal(true);
        organizations.add(uiOrganization);

        return organizations.stream()
                .sorted(Comparator.comparing(UIOrganization::getId,
                        (id1, id2) -> (userId.equals(id1)) ? 1 : (userId.equals(id2)) ? -1 : 0)
                        .thenComparing(UIOrganization::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private UIOrganization getKernelOrganization(String organizationId) {
        String userId = userInfoService.currentUser().getUserId();
        boolean isPersonal = userId.equals(organizationId);

        Organization org = (isPersonal) ? getPersonalOrganization() : organizationStore.find(organizationId);

        if(org == null){
            throw new EntityNotFoundException("Organization not found");
        }

        UIOrganization uiOrganization = UIOrganization.fromKernelOrganization(org, computeDeletionPlanned(org), getUserName(org.getStatusChangeRequesterId()));
        boolean isAdmin = (isPersonal) ? true : userIsAdmin(organizationId);
        uiOrganization.setPersonal(isPersonal);
        uiOrganization.setAdmin(isAdmin);
        return uiOrganization;
    }

    public UIOrganization getOrganizationFromKernel(String organizationId) {
        return fetchOrganizationWithInstances(organizationId);
    }

    public UIOrganization getOrganizationFromKernelWithoutInstances(String organizationId){
        return  getKernelOrganization(organizationId);
    }

    private UIOrganization fetchOrganizationWithInstances(String organizationId) {
        UIOrganization uiOrg = getKernelOrganization(organizationId);
        if (uiOrg.isAdmin())
            uiOrg.setInstances(getOrganizationInstances(uiOrg.getId()));

        return uiOrg;
    }

    private Organization getPersonalOrganization() {
        String userId = userInfoService.currentUser().getUserId();

        Organization org = new Organization();
        org.setId(userId);
        org.setName("Personal");

        return org;
    }

    private List<MyAppsInstance> getOrganizationInstances(String organizationId) {
        UIOrganization uiOrganization = getKernelOrganization(organizationId);
        List<MyAppsInstance> instances = applicationService.getMyInstances(uiOrganization, false);
        return instances;
    }

    private Instant computeDeletionPlanned(Organization organization) {
        Instant possibleDeletionAskedDate =
                organization.getStatus() == OrganizationStatus.DELETED && organization.getStatusChanged() != null
                        ? organization.getStatusChanged() // the user already has clicked on "delete".
                        : Instant.now(); // when the user will click on delete, the deletion planned date
        // will be right even without refreshing the organization first (i.e. passing again in this method).

        return possibleDeletionAskedDate.plus(organizationDaysTillDeletedFromTrash, ChronoUnit.DAYS);
    }


    public List<UIOrganizationMember> getOrganizationMembers(String organizationId, boolean onlyAccepted) {
        UIOrganization uiOrganization = getKernelOrganization(organizationId);
        List<UIOrganizationMember> members = fetchOrganizationMembers(organizationId, uiOrganization.isAdmin());
        if (uiOrganization.isAdmin() && !onlyAccepted) {
            members.addAll(fetchOrganizationPendingMembers(uiOrganization.getId(), uiOrganization.isAdmin()));
        }

        return members;

    }

    private List<UIOrganizationMember> fetchOrganizationMembers(String organizationId, boolean isAdmin) {

        if (isAdmin) {
            // Add organization members :
            return userMembershipService.getMembershipsOfOrganization(organizationId)
                    .stream()
                    .map(membership -> {
                        String email = userProfileService.findUserProfile(membership.getAccountId()).getEmail();

                        UIOrganizationMember uiOrganizationMember = toUIOrganizationMember(membership);
                        uiOrganizationMember.setEmail(email);
                        return uiOrganizationMember;
                    })
                    // NB. self is among returned admins
                    .sorted(Comparator.comparing(UIOrganizationMember::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        } else {
            // return self in first position
            // which was missing : #159 Possibility to see who are the admins of an organization one belongs to
            // followed by admins
            return Stream.concat(
                    Stream.of(selfNonAdminUIOrganizationMember()),
                    userMembershipService.getAdminsOfOrganization(organizationId).stream().map(this::toUIOrganizationMember))
                    // NB. self is already in first position, so ne need to sort
                    .collect(Collectors.toList());
        }
    }


    private List<UIPendingOrganizationMember> fetchOrganizationPendingMembers(String organizationId, boolean isAdmin) {

        if (isAdmin) {
            return userMembershipService
                    .getPendingOrgMembership(organizationId)
                    .stream()
                    .map(this::toUIPendingOrgMembership)
                    // NB. Organize first by admin right, then by email
                    .sorted((member1, member2) -> member1.isAdmin() ? -1 : (member2.isAdmin() ? 1 : member1.getEmail()
                            .compareToIgnoreCase(member2.getEmail())))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Used when non admin, in order to return self in first position
     * which was missing : #159 Possibility to see who are the admins of an organization one belongs to
     */
    private UIOrganizationMember selfNonAdminUIOrganizationMember() {
        UserInfo currentUser = userInfoService.currentUser();
        UIOrganizationMember selfOrgMember = new UIOrganizationMember();
        selfOrgMember.setId(currentUser.getUserId());
        selfOrgMember.setName(currentUser.getNickname());
        selfOrgMember.setAdmin(false);
        selfOrgMember.setSelf(true);
        return selfOrgMember;
    }

    private UIOrganizationMember toUIOrganizationMember(OrgMembership orgMembership) {
        UIOrganizationMember member = new UIOrganizationMember();
        member.setId(orgMembership.getAccountId());
        member.setName(orgMembership.getAccountName());
        member.setAdmin(orgMembership.isAdmin());
        member.setSelf(orgMembership.getAccountId().equals(userInfoService.currentUser().getUserId()));
        return member;
    }

    private UIPendingOrganizationMember toUIPendingOrgMembership(PendingOrgMembership pendingOrgMembership) {
        UIPendingOrganizationMember pMembership = new UIPendingOrganizationMember();
        pMembership.setId(pendingOrgMembership.getId());
        pMembership.setEmail(pendingOrgMembership.getEmail());
        pMembership.setAdmin(pendingOrgMembership.isAdmin());
        pMembership.setPendingMembershipEtag(pendingOrgMembership.getMembershipEtag());
        pMembership.setPendingMembershipUri(pendingOrgMembership.getMembershipUri());

        return pMembership;
    }

    private void updateOrganizationInfo(UIOrganization uiOrganization) {
        Organization org = organizationStore.find(uiOrganization.getId());
        if (shouldUpdateOrg(uiOrganization, org)) {
            org.setName(uiOrganization.getName());
            org.setType(uiOrganization.getType() != null ? uiOrganization.getType() : OrganizationType.PUBLIC_BODY); // though can't change from a functional point of view
            org.setTerritoryId(uiOrganization.getTerritoryId());
            organizationStore.update(org);
        }
    }

    private Optional<OrgMembership> getOrgMembership(String organizationId, String accountId) {
        List<OrgMembership> memberships = userMembershipService.getMembershipsOfOrganization(organizationId);

        return memberships.stream()
                .filter(orgMembership1 -> orgMembership1.getAccountId().equals(accountId))
                .findFirst();
    }

    public void removeMember(String organizationId, String accountId) {
        Optional<OrgMembership> orgMembership = getOrgMembership(organizationId, accountId);

        if (!orgMembership.isPresent()) {
            logger.warn("Membership of account {} was not found in organization {}", accountId, organizationId);
            return;
        }

        userMembershipService.removeMembership(orgMembership.get(), organizationId);
    }

    public void updateMember(String organizationId, String accountId, boolean admin) {
        Optional<OrgMembership> orgMembership = getOrgMembership(organizationId, accountId);

        if (!orgMembership.isPresent()) {
            logger.warn("Membership of account {} was not found in organization {}", accountId, organizationId);
            return;
        }

        logger.debug("Setting admin {} to account {}", admin, accountId);
        userMembershipService.updateMembership(orgMembership.get(), admin, organizationId);
    }

    public UIOrganization setOrganizationStatus(UIOrganization uiOrganization) {
        Organization org = organizationStore.find(uiOrganization.getId());
        if (!userIsAdmin(org.getId())) {
            throw new ForbiddenException();
        }

        boolean statusHasChanged = uiOrganization.getStatus() == null || org.getStatus() == null || !(uiOrganization.getStatus().equals(org.getStatus()));
        if (!statusHasChanged) {
            return uiOrganization;
        }

        /*
            TODO: Modify OrganizationStore.setStatus to return an Organization Object
            see: https://github.com/ozwillo/ozwillo-kernel/blob/c7923c089da49a1a25a502b3ce96e1bc4ab023f8/oasis-webapp/src/main/java/oasis/web/userdirectory/OrganizationEndpoint.java

            TODO: return Organization from response of OrganizationStore.setStatus
         */
        try {
            organizationStore.setStatus(uiOrganization.getId(), uiOrganization.getStatus());
        } catch (HttpClientErrorException e) {
            if (HttpStatus.FORBIDDEN.equals(e.getStatusCode())) {
                throw new ForbiddenException("Cannot delete organization", HttpStatus.FORBIDDEN.value());
            }

            throw e;
        }

        org.setStatus(uiOrganization.getStatus());
        UIOrganization uiOrganizationReturned =  UIOrganization.fromKernelOrganization(org, computeDeletionPlanned(org), getUserName(org.getStatusChangeRequesterId()));
        uiOrganizationReturned.setAdmin(true);
        return uiOrganizationReturned;
    }

    private boolean shouldUpdateOrg(UIOrganization uiOrganization, Organization organization) {
        boolean nameHasChanged = !uiOrganization.getName().equals(organization.getName());
        boolean typeHasChanged = uiOrganization.getType() == null || !(uiOrganization.getType().equals(organization.getType()));
        // NB. status must rather be changed by setStatus()
        boolean territoryIdHasChanged = (uiOrganization.getTerritoryId() == null && organization.getTerritoryId() != null)
                || (uiOrganization.getTerritoryId() != null && organization.getTerritoryId() != null
                && !uiOrganization.getTerritoryId().equals(organization.getTerritoryId()));

        return nameHasChanged || typeHasChanged || territoryIdHasChanged;
    }

    public UserGeneralInfo getCurrentUser() {
        UserInfo userInfo = userInfoService.currentUser();
        return new UserGeneralInfo(userInfo.getGivenName(), userInfo.getFamilyName(), userInfo.getEmail(), userInfo.getAddress());
    }

    private String getUserName(String accountId) {
        if (accountId != null) {
            return userProfileService.findUserProfile(accountId).getDisplayName();
        } else {
            return "";
        }
    }

    /**
     * Verify if the current user is an administrator for a given organization.<br/>
     * NOTE: This does not work for "personal" organization, use rather userIsAdminOrPersonalAppInstance()
     */
    private boolean userIsAdmin(String organizationId) {
        String userId = userInfoService.currentUser().getUserId();

        return userMembershipService.getAdminsOfOrganization(organizationId)
                .stream()
                .anyMatch(orgMembership -> orgMembership.getAccountId().equals(userId));
    }

    public boolean userIsAdminOrPersonalAppInstance(ApplicationInstance existingInstance) {
        return isPersonalAppInstance(existingInstance) || this.userIsAdmin(existingInstance.getProviderId());
    }

    private boolean isPersonalAppInstance(ApplicationInstance existingInstance) {
        return existingInstance.getProviderId() == null;
    }

    public UIPendingOrganizationMember invite(String email, boolean isAdmin, String organizationId) {
        if (!userIsAdmin(organizationId)) {
            logger.error("Potential attack: user {} is not admin of organization {}", userInfoService.currentUser().getUserId(), organizationId);
            throw new ForbiddenException();
        }

        try {
            return userMembershipService.createMembership(email, isAdmin, organizationId, null);
        } catch (WrongQueryException wqex) {
            throw wqex;
        }
    }


    public List<UIPendingOrganizationMember> inviteMultiple(List<InvitationRequest> invitations, String organizationId) {
        if (!userIsAdmin(organizationId)) {
            logger.error("Potential attack: user {} is not admin of organization {}", userInfoService.currentUser().getUserId(), organizationId);
            throw new ForbiddenException();
        }

        Authentication user = user();
        return Flux.fromIterable(invitations)
                .parallel(4)
                .runOn(Schedulers.parallel())
                .map(invitationRequest -> {
                    try {
                        return userMembershipService.createMembership(invitationRequest.getEmail(),
                                invitationRequest.isAdmin(), organizationId, user);
                    } catch (WrongQueryException e) {
                        return null;
                    }
                })
                .sequential()
                .toStream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void removeInvitation(String organizationId, String id, String eTag) {
        // prevent non organization admin users from removing invitations
        if (!userIsAdmin(organizationId)) {
            logger.error("Potential attack: user {} is not admin of organization {}", userInfoService.currentUser()
                    .getUserId(), organizationId);
            throw new ForbiddenException();
        }

        try {
            userMembershipService.removePendingMembership(id, eTag);
        } catch (WrongQueryException wqex) {
            throw wqex;
        }
    }


    public void leave(String organizationId) {
        // prevent the last admin from deleting himself
        if (userIsAdmin(organizationId)) {
            if (userMembershipService.getAdminsOfOrganization(organizationId).size() == 1)
                throw new ForbiddenException();
        }

        userMembershipService.getMembershipsOfUser(userInfoService.currentUser().getUserId()).stream()
                .filter(membership -> membership.getOrganizationId().equals(organizationId))
                .forEach(userMembership -> userMembershipService.removeMembership(userMembership, userInfoService.currentUser().getUserId()));
    }
}
