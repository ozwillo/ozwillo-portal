package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.InstanceACLStore;
import org.oasis_eu.portal.core.model.ace.ACE;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

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

    @Value("${kernel.portal_endpoints.apps}")
    private String endpoint;

    @Override
    public List<ACE> getACL(String instanceId) {

        List<ACE> acl = Arrays.asList(kernel.exchange(endpoint + "/acl/instance/{instanceId}", HttpMethod.GET, null, ACE[].class, user(), instanceId).getBody());

        if (logger.isDebugEnabled()) {
            logger.debug("ACL for instance {}", instanceId);
            acl.stream().forEach(ace -> logger.debug("- {} - {}", ace.getUserId(), ace.getUserName()));
        }
        return acl;

    }

    @Override
    public void saveACL(String instanceId, List<String> newUsers) {
        List<ACE> existingACL = getACL(instanceId);

        // delete the excess ACEs
        existingACL
                .stream()
                .filter(ace -> !newUsers.contains(ace.getUserId()))
                .forEach(ace -> {
                    logger.debug("Deleting ACE {} - {}", ace.getUserId(), ace.getUserName());
                    kernel.exchange(ace.getEntryUri(), HttpMethod.DELETE, new HttpEntity<Object>(ifmatch(ace.getEntryEtag())), Void.class, user());
                });

        // add the new ACEs
        Set<String> currentUsers = existingACL.stream().map(ACE::getUserId).collect(Collectors.toSet());

        newUsers.stream()
                .filter(userid -> !currentUsers.contains(userid))
                .forEach(userid -> {
                    logger.debug("Creating ACE for user {}", userid);
                    kernel.exchange(endpoint + "/acl/instance/{instanceId}", HttpMethod.POST, new HttpEntity<ACE>(ace(userid, instanceId)), ACE.class, user(), instanceId);
                });
    }

    private HttpHeaders ifmatch(String etag) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", etag);
        return headers;
    }

    private ACE ace(String userId, String instanceId) {
        ACE ace = new ACE();
        ace.setUserId(userId);
        ace.setInstanceId(instanceId);
        return ace;
    }
}
