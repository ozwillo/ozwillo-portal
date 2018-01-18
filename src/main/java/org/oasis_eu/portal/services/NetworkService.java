package org.oasis_eu.portal.services;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.model.authority.Authority;
import org.oasis_eu.portal.model.authority.AuthorityType;
import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.portal.model.user.UserGeneralInfo;
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
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    public List<UIOrganization> getMyOrganizations() {
        String userId = userInfoService.currentUser().getUserId();

        return userMembershipService.getMembershipsOfUser(userId)
            .stream()
            .map(this::toUIOrganization)
            .filter(o -> o != null)
            .collect(Collectors.toList());
    }

    private UIOrganization toUIOrganization(UserMembership userMembership) {
        String organizationId = userMembership.getOrganizationId();
        if (organizationId == null) {
            logger.warn("User membership {} has no organization", userMembership.getMembershipUri());
            return null;
        }

        Organization organization = organizationStore.find(organizationId);
        if (organization == null) {
            logger.warn("Unable to retrieve organization {}", organizationId);
            return null;
        }

        UIOrganization org = fillUIOrganization(organization);
        org.setAdmin(userMembership.isAdmin());

        return org;
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
        DateTime possibleDeletionAskedDate = organization.getStatus() == OrganizationStatus.DELETED
            ? new DateTime(organization.getStatusChanged()) // the user already has clicked on "delete".
            : new DateTime(); // when the user will click on delete, the deletion planned date
        // will be right even without refreshing the organization first (i.e. passing again in this method).
        return possibleDeletionAskedDate.plusDays(organizationDaysTillDeletedFromTrash).toInstant();
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
                .map(this::toUIOrganizationMember)
                // NB. self is among returned admins
                .sorted((member1, member2) -> member1.isSelf() ? -1 : (member2.isSelf() ? 1
                    : member1.getNonNullName().compareToIgnoreCase(member2.getNonNullName()))) // #171 old accounts may not have a name before it was required
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

    public String setOrganizationStatus(UIOrganization uiOrganization) {
        Organization org = organizationStore.find(uiOrganization.getId());

        if (!userIsAdmin(org.getId())) {
            throw new ForbiddenException();
        }

        boolean statusHasChanged = uiOrganization.getStatus() == null || org.getStatus() == null || !(uiOrganization.getStatus().equals(org.getStatus()));
        if (statusHasChanged) {
            return organizationStore.setStatus(uiOrganization.getId(), uiOrganization.getStatus());
        }
        return null;
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
                return u1.getFullname() != null && u2.getFullname() != null ? u1.getFullname().compareTo(u2.getFullname()) : 1;
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
     *
     * @param organizationId
     * @return Boolean : True if is organization administrator, False otherwise.
     */
    public boolean userIsAdmin(String organizationId) {
        return userMembershipService.getMembershipsOfUser(userInfoService.currentUser().getUserId()).stream()
            .anyMatch(um -> um.getOrganizationId().equals(organizationId) && um.isAdmin());
    }

    public boolean userIsAdminOrPersonalAppInstance(ApplicationInstance existingInstance) {
        return isPersonalAppInstance(existingInstance) || this.userIsAdmin(existingInstance.getProviderId());
    }

    public boolean isPersonalAppInstance(ApplicationInstance existingInstance) {
        return existingInstance.getProviderId() == null;
    }

    public void invite(String email, String organizationId) {
        if (!userIsAdmin(organizationId)) {
            logger.error("Potential attack: user {} is not admin of organization {}", userInfoService.currentUser().getUserId(), organizationId);
            throw new ForbiddenException();
        }

        try {
            userMembershipService.createMembership(email, organizationId);
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
            userInfoService.currentUser().getUserId(), userInfoService.currentUser().getEmail());

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
