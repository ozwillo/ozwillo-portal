package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.ace.ACE;

import java.util.List;

/**
 * User: schambon
 * Date: 9/16/14
 */
public interface InstanceACLStore {

    List<ACE> getACL(String instanceId);

    void saveACL(String instanceId, List<String> userIds);
}
