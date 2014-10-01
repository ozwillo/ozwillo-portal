package org.oasis_eu.portal.services;

import org.oasis_eu.portal.model.appsmanagement.Authority;
import org.oasis_eu.portal.model.appsmanagement.AuthorityType;
import org.oasis_eu.portal.model.appsmanagement.User;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.Organization;
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
import java.util.Map;
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
            if (userAccount.getName() != null) return userAccount.getName();
            if (userAccount.getGivenName() != null && userAccount.getFamilyName() != null)
                return String.format("%s %s", userAccount.getGivenName(), userAccount.getFamilyName());
            return userAccount.getEmail();
        }
    }

    public Map<String, List<User>> getAgents(List<Authority> authorities) {

        return authorities.stream()
                .collect(Collectors.toMap(Authority::getId, a -> getUsersOfOrganization(a.getId())));
    }

    public void updateAgentStatus(String agentId, String organizationId, boolean admin) {
        // we do NOT allow users to demote themselves
        if (userInfoService.currentUser().getUserId().equals(agentId)) {
            logger.error("Self-modification is forbidden");
            throw new ForbiddenException();
        }

        // check that user is admin of the organization...
        if (!userIsAdmin(organizationId)) {
            logger.error("Potential attack: user {} is not admin of organization {}", userInfoService.currentUser().getUserId(), organizationId);
            throw new ForbiddenException();
        }


        OrgMembership orgMembership = userDirectory.getMembershipsOfOrganization(organizationId).stream().filter(membership -> membership.getAccountId().equals(agentId)).findAny().orElse(null);
        if (orgMembership != null) {
            userDirectory.updateMembership(orgMembership, admin);
        } else {
            logger.error("Cannot find membership for user: {} and organization: {}", agentId, organizationId);
        }
    }

    private boolean userIsAdmin(String organizationId) {
        return userDirectory.getMembershipsOfUser(userInfoService.currentUser().getUserId()).stream()
                .anyMatch(um -> um.getOrganizationId().equals(organizationId) && um.isAdmin());
    }

    public void removeAgentFromOrganization(String agentId, String organizationId) {
        // we do not allow users to remove themselves
        if (userInfoService.currentUser().getUserId().equals(agentId)) {
            logger.error("Self-modification is forbidden");
            throw new ForbiddenException();
        }
        if (!userIsAdmin(organizationId)) {
            logger.error("Potential attack: user {} is not admin of organization {}", userInfoService.currentUser().getUserId(), organizationId);
        }

        OrgMembership orgMembership = userDirectory.getMembershipsOfOrganization(organizationId).stream().filter(membership -> membership.getAccountId().equals(agentId)).findAny().orElse(null);
        if (orgMembership != null) {
            userDirectory.removeMembership(orgMembership);
        } else {
            logger.error("Cannot find membership for user: {} and organization: {}", agentId, organizationId);

        }
    }


    public String getRemoveMessage(String agentId, String organizationId) {
        UserAccount userAccount = userDirectory.findUserAccount(agentId);
        Organization organization = organizationStore.find(organizationId);

        if (userAccount != null && organization != null) {
            return messageSource.getMessage("my.network.confirm-delete.body", new Object[]{userAccount.getName(), organization.getName()}, RequestContextUtils.getLocale(request));
        } else {
            throw new WrongQueryException();
        }
    }


    public void invite(String email, String organizationId) {
        if (! userIsAdmin(organizationId)) {
            logger.error("Potential attack: user {} is not admin of organization {}", userInfoService.currentUser().getUserId(), organizationId);
            throw new ForbiddenException();
        }

        userDirectory.createMembership(email, organizationId);
    }

    public void createOrganization(String name, String type) {
        logger.info("Request to create an organization: {} of type {} from user {} ({})", name, type, userInfoService.currentUser().getUserId(), userInfoService.currentUser().getEmail());

        // TODO implement that when the Kernel supports creating an org and adding oneself as admin.
    }
}
