package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.model.app.instance.MyAppsInstance;
import org.oasis_eu.portal.model.app.service.InstanceService;
import org.oasis_eu.portal.model.authority.Authority;
import org.oasis_eu.portal.model.authority.AuthorityType;
import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.portal.model.user.UserGeneralInfo;
import org.oasis_eu.portal.model.user.UserProfile;
import org.oasis_eu.portal.ui.UIOrganization;
import org.oasis_eu.portal.ui.UIOrganizationMember;
import org.oasis_eu.portal.ui.UIPendingOrganizationMember;
import org.oasis_eu.portal.services.kernel.UserMembershipService;
import org.oasis_eu.portal.services.kernel.UserProfileService;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.*;
import org.oasis_eu.portal.model.organization.OrgMembership;
import org.oasis_eu.portal.model.organization.PendingOrgMembership;
import org.oasis_eu.portal.model.organization.UserMembership;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * User: schambon
 * Date: 9/29/14
 */
@Service
public class NetworkService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);

    @Value("${application.organizationDaysTillDeletedFromTrash:7}")
    private int organizationDaysTillDeletedFromTrash;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserMembershipService userMembershipService;

    @Autowired
    private OrganizationStore organizationStore;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private SubscriptionStore subscriptionStore;

    public List<UIOrganization> getMyOrganizationsInLazyMode() {
        List<UIOrganization> organizations = new ArrayList<>();
        String userId = userInfoService.currentUser().getUserId();

        //build Organizations
        List<UserMembership> userMemberships = userMembershipService.getMembershipsOfUser(userId);
        for (UserMembership u : userMemberships) {
            Organization org = organizationStore.find(u.getOrganizationId());
            UIOrganization uiOrg = fillUIOrganization(org);
            organizations.add(uiOrg);
        }

        UIOrganization uiOrganization = new UIOrganization();
        uiOrganization.setId(userId);
        uiOrganization.setName("Personal");
        organizations.add(uiOrganization);

        return organizations.stream()
                .sorted(Comparator.comparing(UIOrganization::getId,
                        (id1, id2) -> (userId.equals(id1)) ? 1 : (userId.equals(id2))? -1 : 0)
                        .thenComparing(Comparator.comparing(UIOrganization::getName, String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());
    }

    public List<UIOrganization> getMyOrganizations() {
        List<UIOrganization> organizations = new ArrayList<>();
        String userId = userInfoService.currentUser().getUserId();

        //Fetch all user's services
        List<Subscription> subs = subscriptionStore.findByUserId(userId);
        Map<String, List<InstanceService>> instanceServices = subs
                .stream()
                .map(this::getServiceBySub)
                .filter(sub -> sub != null)
                .filter(is -> is.getCatalogEntry().getProviderId() != null)
                .collect(Collectors.groupingBy(is -> is.getCatalogEntry().getProviderId()));

        //build Organizations
        List<UserMembership> userMemberships = userMembershipService.getMembershipsOfUser(userId);
        for (UserMembership u : userMemberships) {
            Organization org = organizationStore.find(u.getOrganizationId());
            UIOrganization uiOrg = fillUIOrganization(org);
            uiOrg.setServices(instanceServices.get(org.getId()));
            uiOrg.setAdmin(userIsAdmin(org.getId()));
            organizations.add(uiOrg);
        }

        UIOrganization uiOrganization = new UIOrganization();
        uiOrganization.setId(userId);
        uiOrganization.setName("Personal");
        uiOrganization.setServices(instanceServices.get(userId));
        organizations.add(uiOrganization);

        return organizations.stream()
                .sorted(Comparator.comparing(UIOrganization::getId,
                        (id1, id2) -> (userId.equals(id1)) ? 1 : (userId.equals(id2))? -1 : 0)
                .thenComparing(Comparator.comparing(UIOrganization::getName, String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());
    }


    private InstanceService getServiceBySub(Subscription sub) {
        try {
            return applicationService.getService(sub.getServiceId());
        } catch (WrongQueryException e) {
            if (HttpStatus.FORBIDDEN.value() != e.getStatusCode()) {
                throw e;
            }
            return null;
        }
    }

    public UIOrganization getOrganization(String organizationId) {
        String userId = userInfoService.currentUser().getUserId();

        // Personal organization
        if (userId.equals(organizationId)) {
            return fetchOrganizationWithInstances(organizationId, false);
        }

        return fetchOrganizationWithInstances(organizationId, true);
    }

    private UIOrganization fetchOrganizationWithInstances(String organizationId, boolean fetchMembers) {
        String userId = userInfoService.currentUser().getUserId();
        boolean isPersonal = userId.equals(organizationId);

        Organization org = (isPersonal)? getPersonalOrganization() : organizationStore.find(organizationId);

        //Build UIOrganization
        UIOrganization uiOrg = fillUIOrganization(org);
        boolean isAdmin = userIsAdmin(organizationId);
        uiOrg.setAdmin(isAdmin);
        if (isAdmin)
            uiOrg.setInstances(getOrganizationInstances(org.getId(), uiOrg.isAdmin()));

        if(fetchMembers && !isPersonal) {
            List<UIOrganizationMember> members = getOrganizationMembers(org.getId());
            if (isAdmin)
               members.addAll(getOrganizationPendingMembers(org.getId()));
            uiOrg.setMembers(members);
        }

        return uiOrg;
    }

    private Organization getPersonalOrganization() {
        String userId = userInfoService.currentUser().getUserId();

        Organization org = new Organization();
        org.setId(userId);
        org.setName("Personal");

        return org;
    }

    private List<MyAppsInstance> getOrganizationInstances(String organizationId, boolean isAdmin) {
        Authority authority = getOrganizationAuthority(organizationId);
        List<MyAppsInstance> instances = applicationService.getMyInstances(authority, true);

        //Fetch subscriptions
        if (isAdmin) {
            instances.forEach(instance -> {
                instance.getServices().forEach(s -> {
                    s.setSubscriptions(subscriptionStore.findByServiceId(s.getCatalogEntry().getId()));
                });
            });
        }

        return instances;
    }

    private UIOrganization fillUIOrganization(Organization organization) {
        UIOrganization uiOrg = new UIOrganization();
        uiOrg.setId(organization.getId());
        uiOrg.setName(organization.getName());
        uiOrg.setType(organization.getType());
        if (organization.getTerritoryId() != null) {
            uiOrg.setTerritoryId(organization.getTerritoryId());
            uiOrg.setTerritoryLabel(String.valueOf(organization.getTerritoryId())); // TODO if any get label from cache with user locale (?????????!!!!!!!!!!!!!!!!)
        }
        uiOrg.setDcId(organization.getDcId());
        uiOrg.setStatus(organization.getStatus());
        uiOrg.setStatusChanged(organization.getStatusChanged());
        uiOrg.setDeletionPlanned(computeDeletionPlanned(organization));

        if (organization.getStatusChangeRequesterId() != null) {
            uiOrg.setStatusChangeRequesterId(organization.getStatusChangeRequesterId());
            uiOrg.setStatusChangeRequesterLabel(getUserName(organization.getStatusChangeRequesterId(), null)); // TODO protected ?? then from membership
        }

        return uiOrg;
    }

    private Instant computeDeletionPlanned(Organization organization) {
        Instant possibleDeletionAskedDate =
                organization.getStatus() == OrganizationStatus.DELETED && organization.getStatusChanged() != null
            ? organization.getStatusChanged() // the user already has clicked on "delete".
            : Instant.now(); // when the user will click on delete, the deletion planned date
        // will be right even without refreshing the organization first (i.e. passing again in this method).

        return possibleDeletionAskedDate.plus(organizationDaysTillDeletedFromTrash, ChronoUnit.DAYS);
    }

    public List<UIOrganizationMember> getOrganizationMembers(String organizationId) {
        UserInfo currentUser = userInfoService.currentUser();
        List<OrgMembership> orgAdmins = userMembershipService.getAdminsOfOrganization(organizationId);
        boolean isAdmin =
            orgAdmins.stream().anyMatch(orgMembership -> orgMembership.getAccountId().equals(currentUser.getUserId()));

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
                orgAdmins.stream().map(this::toUIOrganizationMember))
                // NB. self is already in first position, so ne need to sort
                .collect(Collectors.toList());
        }
    }


    public List<UIPendingOrganizationMember> getOrganizationPendingMembers(String organizationId) {
        UserInfo currentUser = userInfoService.currentUser();
        List<OrgMembership> orgAdmins = userMembershipService.getAdminsOfOrganization(organizationId);
        boolean isAdmin =
            orgAdmins.stream().anyMatch(orgMembership -> orgMembership.getAccountId().equals(currentUser.getUserId()));

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
            return null;
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

    public void updateOrganizationInfo(UIOrganization uiOrganization) {
        Organization org = organizationStore.find(uiOrganization.getId());
        if (shouldUpdateOrg(uiOrganization, org)) {
            org.setName(uiOrganization.getName());
            org.setType(uiOrganization.getType() != null ? uiOrganization.getType() : OrganizationType.PUBLIC_BODY); // though can't change from a functional point of view
            org.setTerritoryId(uiOrganization.getTerritoryId());
            //org.setStatus(uiOrganization.getStatus()); // NB. status must rather be changed by setStatus()
            // NB. status' changed / requester can't be modified by portal

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
            if( HttpStatus.FORBIDDEN.equals(e.getStatusCode()) ) {
                String translatedBusinessMessage = messageSource.getMessage("error.msg.delete-organization",
                        new Object[]{}, RequestContextUtils.getLocale(request));
                throw new ForbiddenException(translatedBusinessMessage, HttpStatus.FORBIDDEN.value());
            }

            throw e;
        }

        org.setStatus(uiOrganization.getStatus());
        return fillUIOrganization(org);
    }

    /**
     * @param uiOrganization
     * @param organization
     * @return
     */
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
        UserGeneralInfo userGeneralInfo = new UserGeneralInfo(userInfo.getGivenName(), userInfo.getFamilyName(), userInfo.getEmail(), userInfo.getAddress());
        return userGeneralInfo;
    }

    public List<Authority> getMyAuthorities(boolean includePersonal) {
        String userId = userInfoService.currentUser().getUserId();

        List<Authority> authorities = new ArrayList<>();
        if (includePersonal) {
            authorities.add(new Authority(AuthorityType.INDIVIDUAL, i18nPersonal(), userId, true));
        }

        authorities.addAll(userMembershipService.getMembershipsOfUser(userId)
            .stream()
            .filter(UserMembership::isAdmin)
            .map(this::toAuthority)
            .collect(Collectors.toList()));

        Collections.sort(authorities, (one, two) -> {
            if (one.getType().ordinal() != two.getType().ordinal())
                return one.getType().ordinal() - two.getType().ordinal();
            else return one.getName().compareTo(two.getName());
        });

        return authorities;
    }

    public Authority getOrganizationAuthority(String organizationId) {
        String userId = userInfoService.currentUser().getUserId();

        if (userId.equals(organizationId)) {
            return new Authority(AuthorityType.INDIVIDUAL, i18nPersonal(), userId, true);
        }

        List<UserMembership> orgMemberships = userMembershipService.getMembershipsOfUser(userId);
        UserMembership currentMember = null;
        for(UserMembership member : orgMemberships) {
            if(member.getOrganizationId().equals(organizationId)){
                currentMember = member;
                break;
            }
        }

        return (currentMember != null)? toAuthority(currentMember) : null;
    }

    private Authority toAuthority(UserMembership userMembership) {
        return new Authority(AuthorityType.ORGANIZATION, userMembership.getOrganizationName(), userMembership.getOrganizationId(), userMembership.isAdmin());
    }

    public Authority getAuthority(String authorityType, String authorityId) {
        switch (AuthorityType.valueOf(authorityType)) {
            case INDIVIDUAL:
                return new Authority(AuthorityType.INDIVIDUAL, i18nPersonal(), userInfoService.currentUser().getUserId(), true); // in this case, discard the provided argument

            case ORGANIZATION:
                // note: at the risk of being a bit slow, we're checking that the user has membership of this org
                UserMembership um = userMembershipService.getMembershipsOfUser(userInfoService.currentUser().getUserId())
                    .stream()
                    .filter(m -> m.getOrganizationId().equals(authorityId))
                    .findFirst()
                    .orElse(null);

                return new Authority(AuthorityType.ORGANIZATION, um.getOrganizationName(), authorityId, um.isAdmin());
        }

        return null;
    }

    private String i18nPersonal() {
        return messageSource.getMessage("my.apps.personal", new Object[0], RequestContextUtils.getLocale(request));
    }

    public List<User> getUsersOfOrganization(String organizationId) {
        return userMembershipService.getMembershipsOfOrganization(organizationId)
            .stream()
            .map(m -> new User(m.getAccountId(), getUserName(m.getAccountId(), m.getAccountName()), m.isAdmin()))
            .sorted((u1, u2) -> {
                if (u1.getUserid().equals(userInfoService.currentUser().getUserId())) return -1;
                if (u2.getUserid().equals(userInfoService.currentUser().getUserId())) return 1;
                if (u1.isAdmin()) return -1;
                if (u2.isAdmin()) return 1;
                return u1.getName() != null && u2.getName() != null ? u1.getName().compareTo(u2.getName()) : 1;
            })
            .collect(Collectors.toList());
    }

    /**
     * reused by PortalAppManagementService for instance trash mode
     *
     */
    public String getUserName(String accountId, String accountName) {
        if (accountName != null) {
            return accountName;
        } else {
            return userProfileService.findUserProfile(accountId).getDisplayName();
        }
    }

    /**
     * Verify if the current user is an administrator for a given organization.<br/>
     * NOTE: This does not work for "personal" organization, use rather userIsAdminOrPersonalAppInstance()
     */
    public boolean userIsAdmin(String organizationId) {
        String userId = userInfoService.currentUser().getUserId();

        return userMembershipService.getMembershipsOfUser(userId).stream()
            .anyMatch(um -> um.getOrganizationId().equals(organizationId) && um.isAdmin());
    }

    public boolean userIsAdminOrPersonalAppInstance(ApplicationInstance existingInstance) {
        return isPersonalAppInstance(existingInstance) || this.userIsAdmin(existingInstance.getProviderId());
    }

    public boolean isPersonalAppInstance(ApplicationInstance existingInstance) {
        return existingInstance.getProviderId() == null;
    }

    public UIPendingOrganizationMember invite(String email, boolean isAdmin, String organizationId) {
        if (!userIsAdmin(organizationId)) {
            logger.error("Potential attack: user {} is not admin of organization {}", userInfoService.currentUser().getUserId(), organizationId);
            throw new ForbiddenException();
        }

        try {
            return userMembershipService.createMembership(email, isAdmin, organizationId);
        } catch (WrongQueryException wqex) {
            if (wqex.getStatusCode() == org.springframework.http.HttpStatus.CONFLICT.value()) {
                // Translated msg. see issue #201
                String translatedBusinessMessage = messageSource.getMessage("error.msg.user-already-invited",
                    new Object[]{}, RequestContextUtils.getLocale(request));
                wqex.setTranslatedBusinessMessage(translatedBusinessMessage);
            }
            throw wqex;
        }
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
            if (wqex.getStatusCode() == org.springframework.http.HttpStatus.CONFLICT.value()) {
                String translatedBusinessMessage = messageSource.getMessage("error.msg.data-conflict",
                        new Object[]{}, RequestContextUtils.getLocale(request));
                wqex.setTranslatedBusinessMessage(translatedBusinessMessage);
            }
            throw wqex;
        }
    }


    public void leave(String organizationId) {
        // prevent the last admin from deleting themselves
        if (userIsAdmin(organizationId)) {
            if (userMembershipService.getMembershipsOfOrganization(organizationId).stream().filter(OrgMembership::isAdmin).count() == 1) {
                throw new ForbiddenException();
            }
        }

        userMembershipService.getMembershipsOfUser(userInfoService.currentUser().getUserId()).stream()
            .filter(membership -> membership.getOrganizationId().equals(organizationId))
            .forEach(userMembership -> userMembershipService.removeMembership(userMembership, userInfoService.currentUser().getUserId()));
    }

    public UIOrganization searchOrganizationByDCId(String dcIc) {
        // Search for existing organization having "GET /d/org?dc_id=xx"
        Organization org = organizationStore.findByDCID(dcIc);
        return org != null ? fillUIOrganization(org) : null;
    }

    public UIOrganization searchOrganizationByDCIdAndAliases(List<String> dcIds) {
        // Search for existing organization having "GET /d/org?dc_id=xx"
        Organization organization = null;
        for (String dcId : dcIds) {
            organization = organizationStore.findByDCID(dcId);
            if (organization != null)
                break;
        }

        return organization != null ? fillUIOrganization(organization) : null;
    }

    public UIOrganization createOrganization(String name, String type, URI territoryId, URI dcId) {
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

        UIOrganization result = fillUIOrganization(org);
        result.setAdmin(true);

        return result;
    }
}
