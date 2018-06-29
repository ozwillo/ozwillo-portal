package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.InstanceACLStore;
import org.oasis_eu.portal.core.model.ace.ACE;
import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

/**
 * User: schambon
 * Date: 9/16/14
 */
@Component
public class InstanceACLStoreImpl implements InstanceACLStore {

    private static Logger logger = LoggerFactory.getLogger(InstanceACLStoreImpl.class);

    @Autowired
    private Kernel kernel;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MessageSource messageSource;

    @Value("${kernel.portal_endpoints.apps}")
    private String endpoint;

    @Override
    public List<ACE> getACL(String instanceId) {
        List<ACE> acl = Arrays.asList(kernel.getEntityOrException(endpoint + "/acl/instance/{instanceId}",
            ACE[].class, user(), instanceId));
        if (logger.isDebugEnabled()) {
            logger.debug("ACL for instance {}", instanceId);
            acl.stream().forEach(ace -> logger.debug("- {} - {}", ace.getUserId(), ace.getUserName()));
        }

        return acl; // including !app_user app_admin

    }

    @Override
    public List<ACE> getPendingACL(String instanceId) {
        List<ACE> acl = Arrays.asList(kernel.getEntityOrException(endpoint + "/pending-acl/instance/{instanceId}",
            ACE[].class, user(), instanceId));
        if (logger.isDebugEnabled()) {
            logger.debug("ACL for instance {}", instanceId);
            acl.stream().forEach(ace -> logger.debug("- {}", ace.getEmail()));
        }

        return acl;
    }


    @Override
    public void createACL(String instanceId, User user) {
        List<ACE> existingACL = getACL(instanceId);

        // delete the excess ACEs
        for(ACE ace : existingACL) {
            if(ace.getUserId().equals(user.getUserid()) && ace.isAppUser()) {
                String translatedBusinessMessage = messageSource.getMessage("error.msg.acl-already-exist",
                        new Object[]{}, RequestContextUtils.getLocale(request));
                throw new ForbiddenException(translatedBusinessMessage, HttpStatus.FORBIDDEN.value());
            }

        }

        logger.debug("Creating ACE for user {} - {}", user.getUserid(), user.getEmail());
        kernel.exchange(endpoint + "/acl/instance/{instanceId}", HttpMethod.POST,
                new HttpEntity<>(ace(user.getUserid(), null, instanceId)), ACE.class, user(), instanceId);
    }


    @Override
    public void createACL(String instanceId, String email) {
        List<ACE> existingPendingACL = getPendingACL(instanceId);

        for(ACE ace : existingPendingACL) {
            if(ace.getEmail().equals(email)) {
                String translatedBusinessMessage = messageSource.getMessage("error.msg.acl-already-exist",
                        new Object[]{}, RequestContextUtils.getLocale(request));
                throw new ForbiddenException(translatedBusinessMessage, HttpStatus.FORBIDDEN.value());
            }

        }

        logger.debug("Creating pending ACE for {} ", email);
        kernel.exchange(endpoint + "/acl/instance/{instanceId}", HttpMethod.POST,
                new HttpEntity<>(ace(null, email, instanceId)), ACE.class, user(), instanceId);
    }

    @Override
    public void deleteACL(String instanceId, User user) {
        getACL(instanceId)
            .stream()
            .filter(ace -> ace.getUserId().equals(user.getUserid()))
            .forEach(ace -> {
                logger.debug("Deleting ACE for {} ", ace.getEmail());
                kernel.exchange(ace.getEntryUri(), HttpMethod.DELETE,
                        new HttpEntity<>(ifmatch(ace.getEntryEtag())), Void.class, user(), ace.getId());
            });
    }

    @Override
    public void deleteACL(String instanceId, String email) {
        getPendingACL(instanceId)
                .stream()
                .filter(ace -> ace.getEmail().equals(email))
                .forEach(ace -> {
                    logger.debug("Deleting pending ACE for {} ", ace.getEmail());
                    kernel.exchange(ace.getPendingEntryUri(), HttpMethod.DELETE,
                            new HttpEntity<>(ifmatch(ace.getPendingEntryEtag())), Void.class, user(), ace.getId());
                });
    }

    @Override
    public void saveACL(String instanceId, List<User> users) {
        List<ACE> existingACL = getACL(instanceId);
        List<ACE> existingPendingACL = getPendingACL(instanceId);
        List<String> newUsersIds = users.stream()
            .filter(user -> user.getUserid() != null)
            .map(User::getUserid)
            .collect(Collectors.toList());
        List<String> newUsersEmails = users.stream()
            .filter(user -> user.getEmail() != null)
            .map(User::getEmail)
            .collect(Collectors.toList());

        // NB. ACLs that are app_admin !app_user are filtered out because NOT handled here
        // (Kernel deduces them from app orga admins)

        // delete the excess ACEs
        existingACL.stream()
            .filter(ace -> !newUsersIds.contains(ace.getUserId()))
            .filter(ACE::isAppUser) // #157 filter out when (app_admin and) not app_user (and entry_uri null anyway so couldn't delete)
            .forEach(ace -> {
                logger.debug("Deleting ACE {} - {}", ace.getUserId(), ace.getUserName());
                kernel.exchange(ace.getEntryUri(), HttpMethod.DELETE, new HttpEntity<>(ifmatch(ace.getEntryEtag())), Void.class, user());
            });

        existingPendingACL.stream()
            .filter(ace -> !newUsersEmails.contains(ace.getEmail()))
            .forEach(ace -> {
                logger.debug("Deleting pending ACE for {}", ace.getEmail());
                kernel.exchange(ace.getPendingEntryUri(), HttpMethod.DELETE, new HttpEntity<>(ifmatch(ace.getPendingEntryEtag())), Void.class, user());
            });

        // add the new ACEs
        Set<String> currentUsersIds = existingACL.stream()
            .filter(ACE::isAppUser) // #157 filter out when (app_admin and) not app_user (else can't make an app_admin become app_user)
            .map(ACE::getUserId).collect(Collectors.toSet());
        Set<String> currentUsersEmails = existingPendingACL.stream()
            .map(ACE::getEmail).collect(Collectors.toSet());

        users.stream()
            .filter(user -> !currentUsersIds.contains(user.getUserid()) && !currentUsersEmails.contains(user.getEmail())) // only on not existing users (NB. can be already app_admin)
            .forEach(user -> {
                logger.debug("Creating ACE for user {} - {}", user.getUserid(), user.getEmail());
                kernel.exchange(endpoint + "/acl/instance/{instanceId}", HttpMethod.POST,
                        new HttpEntity<>(ace(user.getUserid(), user.getEmail(), instanceId)), ACE.class, user(), instanceId);
            });

    }

    private HttpHeaders ifmatch(String etag) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", etag);
        return headers;
    }

    private ACE ace(String userId, String email, String instanceId) {
        ACE ace = new ACE();
        ace.setUserId(userId);
        ace.setEmail(email);
        ace.setInstanceId(instanceId);
        ace.setAppUser(true); // #157 (but overwritten by Kernel anyway)
        //ace.setAppAdmin(appAdmin); // #157 LATER when there'll be app admins managed by orga admins (for now the same)
        return ace;
    }
}
