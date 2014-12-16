package org.oasis_eu.portal.services;

import org.oasis_eu.portal.model.appsmanagement.Authority;
import org.oasis_eu.portal.model.appsmanagement.AuthorityType;
import org.oasis_eu.portal.model.appsmanagement.User;
import org.oasis_eu.portal.model.network.UIOrganization;
import org.oasis_eu.portal.model.network.UIOrganizationMember;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.model.Organization;
import org.oasis_eu.spring.kernel.model.OrganizationType;
import org.oasis_eu.spring.kernel.model.UserAccount;
import org.oasis_eu.spring.kernel.model.directory.OrgMembership;
import org.oasis_eu.spring.kernel.model.directory.UserMembership;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 9/29/14
 */
@Service
public class NetworkService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkService.class);

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserDirectory userDirectory;

    @Autowired
    private OrganizationStore organizationStore;

    public List<UIOrganization> getMyOrganizations() {
        String userId = userInfoService.currentUser().getUserId();

        return userDirectory.getMembershipsOfUser(userId)
                .stream()
                .map(this::toUIOrganization)
                .filter(o -> o != null)
                .collect(Collectors.toList());
    }

    private UIOrganization toUIOrganization(UserMembership userMembership) {
        UIOrganization org = new UIOrganization();
        String organizationId = userMembership.getOrganizationId();
        if (organizationId == null) {
            return null;
        }
        org.setId(organizationId);
        String organizationName = userMembership.getOrganizationName();
        if (organizationName == null) {
            return null;
        }
        org.setName(organizationName);
        org.setAdmin(userMembership.isAdmin());

        Organization organization = organizationStore.find(organizationId);
        if (organization != null) {
            org.setType(organization.getType());
        } else {
            return null;
        }

        if (userMembership.isAdmin()) {
            org.setMembers(userDirectory.getMembershipsOfOrganization(organizationId).stream()
                    .map(this::toUIOrganizationMember)
                    .sorted((member1, member2) -> member1.isSelf() ? -1 : (member2.isSelf() ? 1 : member1.getName().compareToIgnoreCase(member2.getName())))
                    .collect(Collectors.toList()));
        } else {
            org.setMembers(userDirectory.getAdminsOfOrganization(organizationId).stream()
                            .map(this::toUIOrganizationMember)
                            .sorted((member1, member2) -> member1.isSelf() ? -1 : (member2.isSelf() ? 1 : member1.getName().compareToIgnoreCase(member2.getName())))
                            .collect(Collectors.toList())
            );
        }

        return org;
    }

    private UIOrganizationMember toUIOrganizationMember(OrgMembership orgMembership) {
        UIOrganizationMember member = new UIOrganizationMember();
        member.setId(orgMembership.getAccountId());
        member.setName(orgMembership.getAccountName());
        member.setAdmin(orgMembership.isAdmin());
        member.setSelf(orgMembership.getAccountId().equals(userInfoService.currentUser().getUserId()));
        return member;
    }


    public void updateOrganization(UIOrganization uiOrganization) {
        Organization org = organizationStore.find(uiOrganization.getId());
        if (shouldUpdateOrg(uiOrganization, org)) {
            org.setName(uiOrganization.getName());
            org.setType(uiOrganization.getType() != null ? uiOrganization.getType() : OrganizationType.PUBLIC_BODY);

            organizationStore.update(org);
        }

        List<OrgMembership> memberships = userDirectory.getMembershipsOfOrganization(uiOrganization.getId());

        // note: there can be no added users (we invite them by email directly)

        // find the members to remove
        memberships.stream().filter(om ->
                        uiOrganization.getMembers().stream().noneMatch(member -> om.getAccountId().equals(member.getId()))
        ).forEach(om -> userDirectory.removeMembership(om, uiOrganization.getId()));

        // then the members to change (note: we only change the "admin" flag for now)
        memberships.stream().filter(om ->
                        uiOrganization.getMembers().stream().anyMatch(member -> om.getAccountId().equals(member.getId()) && (member.isAdmin() != om.isAdmin()))
        ).forEach(om -> userDirectory.updateMembership(om, !om.isAdmin(), uiOrganization.getId()));


    }

    private boolean shouldUpdateOrg(UIOrganization uiOrganization, Organization organization) {
        boolean nameHasChanged = !uiOrganization.getName().equals(organization.getName());
        boolean typeHasChanged = uiOrganization.getType() == null || organization.getType() == null || !(uiOrganization.getType().equals(organization.getType()));

        return nameHasChanged || typeHasChanged;
    }


    public List<Authority> getMyAuthorities(boolean includePersonal) {
        String userId = userInfoService.currentUser().getUserId();

        List<Authority> authorities = new ArrayList<>();
        if (includePersonal) {
            authorities.add(new Authority(AuthorityType.INDIVIDUAL, i18nPersonal(), userId, true));
        }

        authorities.addAll(userDirectory.getMembershipsOfUser(userId)
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

    private Authority toAuthority(UserMembership userMembership) {
        return new Authority(AuthorityType.ORGANIZATION, userMembership.getOrganizationName(), userMembership.getOrganizationId(), userMembership.isAdmin());
    }

    public Authority getAuthority(String authorityType, String authorityId) {
        switch(AuthorityType.valueOf(authorityType)) {
            case INDIVIDUAL:
                return new Authority(AuthorityType.INDIVIDUAL, i18nPersonal(), userInfoService.currentUser().getUserId(), true); // in this case, discard the provided argument

            case ORGANIZATION:
                // note: at the risk of being a bit slow, we're checking that the user has membership of this org
                UserMembership um = userDirectory.getMembershipsOfUser(userInfoService.currentUser().getUserId())
                        .stream()
                        .filter(m -> m.getOrganizationId().equals(authorityId))
                        .findFirst()
                        .orElse(null);
                return new Authority(AuthorityType.ORGANIZATION, organizationStore.find(authorityId).getName(), authorityId, um.isAdmin());
        }

        return null;
    }

    private String i18nPersonal() {
        return messageSource.getMessage("my.apps.personal", new Object[0], RequestContextUtils.getLocale(request));
    }

    public List<User> getUsersOfOrganization(String organizationId) {
        return userDirectory.getMembershipsOfOrganization(organizationId)
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

    private String getUserName(String accountId, String accountName) {
        if (accountName != null) {
            return accountName;
        } else {
            UserAccount userAccount = userDirectory.findUserAccount(accountId);
            if (userAccount.getNickname() != null) return userAccount.getNickname();
            if (userAccount.getName() != null) return userAccount.getName();
            if (userAccount.getGivenName() != null && userAccount.getFamilyName() != null)
                return String.format("%s %s", userAccount.getGivenName(), userAccount.getFamilyName());
            return userAccount.getEmail() != null ? userAccount.getEmail() : accountId;
        }
    }

    public boolean userIsAdmin(String organizationId) {
        return userDirectory.getMembershipsOfUser(userInfoService.currentUser().getUserId()).stream()
                .anyMatch(um -> um.getOrganizationId().equals(organizationId) && um.isAdmin());
    }


    public void invite(String email, String organizationId) {
        if (! userIsAdmin(organizationId)) {
            logger.error("Potential attack: user {} is not admin of organization {}", userInfoService.currentUser().getUserId(), organizationId);
            throw new ForbiddenException();
        }

        userDirectory.createMembership(email, organizationId);
    }


    public void leave(String organizationId) {
        // prevent the last admin from deleting themselves
        if (userIsAdmin(organizationId)) {
            if (userDirectory.getMembershipsOfOrganization(organizationId).stream().filter(OrgMembership::isAdmin).count() == 1) {
                throw new ForbiddenException();
            }
        }

        userDirectory.getMembershipsOfUser(userInfoService.currentUser().getUserId()).stream()
                .filter(membership -> membership.getOrganizationId().equals(organizationId))
                .forEach(userMembership -> userDirectory.removeMembership(userMembership, userInfoService.currentUser().getUserId()));
    }

    public UIOrganization createOrganization(String name, String type) {
        logger.info("Request to create an organization: {} of type {} from user {} ({})", name, type, userInfoService.currentUser().getUserId(), userInfoService.currentUser().getEmail());

        Organization org = new Organization();
        org.setName(name);
        org.setType(OrganizationType.valueOf(type));  // throws an IllegalArgumentException if the type isn't provided

        org = organizationStore.create(org);

        UIOrganization result = new UIOrganization();
        result.setId(org.getId());
        result.setType(org.getType());
        result.setName(org.getName());
        result.setAdmin(true);
        result.setMembers(Collections.emptyList());

        return result;
    }

    public void deleteOrganization(String organizationId) {
        if (!userIsAdmin(organizationId)) {
            throw new ForbiddenException();
        }
        organizationStore.delete(organizationId);
    }
}
