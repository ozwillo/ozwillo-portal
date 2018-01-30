package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.ace.ACE;
import org.oasis_eu.portal.model.user.User;

import java.util.List;

/**
 * User: schambon
 * Date: 9/16/14
 */
public interface InstanceACLStore {

    /**
     * Lists ALL ACE (including app_admin !app_user which Kernel actually deduces from orga admins)
     *
     * @param instanceId
     * @return
     */
    List<ACE> getACL(String instanceId);

    /**
     * Lists pending ACEs for a given instance.
     * <p>
     * Pending ACEs are users invited by email who have not yep accepted the invitation
     */
    List<ACE> getPendingACL(String instanceId);

    /**
     * Saves ACLs, for now only where app_user
     * (since for those that are app_admin !app_user, Kernel deduces them from app orga admins)
     *
     * @param instanceId
     * @param userIds
     */
    void saveACL(String instanceId, List<User> userIds);

    /**
     * Create ACL to allow an user to use an instance.
     *
     * @param instanceId
     * @param user
     */
    void createACL(String instanceId, User user);

    /**
     * Create ACL to invite a future user to use an instance.
     *
     * @param instanceId
     * @param email
     */
    void createACL(String instanceId, String email);

    /**
     * Delete ACL
     *
     * @param instanceId
     * @param user
     */
    void deleteACL(String instanceId, User user);

    /**
     * Delete ACL sent by email
     *
     * @param instanceId
     * @param email
     */
    void deleteACL(String instanceId, String email);
}
