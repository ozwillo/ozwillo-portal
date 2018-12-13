package org.oasis_eu.portal.services.kernel;

import org.oasis_eu.portal.model.kernel.instance.ACE;
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

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

@Component
public class InstanceACLStoreImpl {

    private static Logger logger = LoggerFactory.getLogger(InstanceACLStoreImpl.class);

    @Autowired
    private Kernel kernel;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MessageSource messageSource;

    @Value("${kernel.portal_endpoints.apps}")
    private String endpoint;

    public List<ACE> getACL(String instanceId) {
        List<ACE> acl = Arrays.asList(kernel.getEntityOrException(endpoint + "/acl/instance/{instanceId}",
            ACE[].class, user(), instanceId));
        if (logger.isDebugEnabled()) {
            logger.debug("ACL for instance {}", instanceId);
            acl.forEach(ace -> logger.debug("- {} - {}", ace.getUserId(), ace.getUserName()));
        }

        return acl; // including !app_user app_admin

    }

    public List<ACE> getPendingACL(String instanceId) {
        List<ACE> acl = Arrays.asList(kernel.getEntityOrException(endpoint + "/pending-acl/instance/{instanceId}",
            ACE[].class, user(), instanceId));
        if (logger.isDebugEnabled()) {
            logger.debug("ACL for instance {}", instanceId);
            acl.stream().forEach(ace -> logger.debug("- {}", ace.getEmail()));
        }

        return acl;
    }

    public void createACLForMember(String instanceId, String userId) {
        List<ACE> existingACL = getACL(instanceId);

        // delete the excess ACEs
        for(ACE ace : existingACL) {
            if(ace.getUserId().equals(userId) && ace.isAppUser()) {
                throw new ForbiddenException("Acl already exist", HttpStatus.FORBIDDEN.value());
            }

        }

        logger.debug("Creating ACE for user {}", userId);
        kernel.exchange(endpoint + "/acl/instance/{instanceId}", HttpMethod.POST,
                new HttpEntity<>(ace(userId, null, instanceId)), ACE.class, user(), instanceId);
    }

    public void createACLForExternal(String instanceId, String email) {
        List<ACE> existingPendingACL = getPendingACL(instanceId);

        for(ACE ace : existingPendingACL) {
            if(ace.getEmail().equals(email)) {
                throw new ForbiddenException("Acl ready exist", HttpStatus.FORBIDDEN.value());
            }

        }

        logger.debug("Creating pending ACE for {} ", email);
        kernel.exchange(endpoint + "/acl/instance/{instanceId}", HttpMethod.POST,
                new HttpEntity<>(ace(null, email, instanceId)), ACE.class, user(), instanceId);
    }

    public void deleteACL(String instanceId, String userId) {
        getACL(instanceId)
            .stream()
            .filter(ace -> ace.getUserId().equals(userId) && ace.getEntryUri() != null)
            .forEach(ace -> {
                logger.debug("Deleting ACE for {} ", ace.getEmail());
                kernel.exchange(ace.getEntryUri(), HttpMethod.DELETE,
                        new HttpEntity<>(ifmatch(ace.getEntryEtag())), Void.class, user(), ace.getId());
            });
    }

    public void deletePendingACL(String instanceId, String email) {
        getPendingACL(instanceId)
                .stream()
                .filter(ace -> ace.getEmail().equals(email))
                .forEach(ace -> {
                    logger.debug("Deleting pending ACE for {} ", ace.getEmail());
                    kernel.exchange(ace.getPendingEntryUri(), HttpMethod.DELETE,
                            new HttpEntity<>(ifmatch(ace.getPendingEntryEtag())), Void.class, user(), ace.getId());
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
