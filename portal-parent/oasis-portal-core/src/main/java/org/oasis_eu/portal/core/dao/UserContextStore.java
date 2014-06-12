package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.subscription.UserContext;

import java.util.List;

/**
 * User: schambon
 * Date: 6/12/14
 */
public interface UserContextStore {

    UserContext addUserContext(String userId, UserContext userContext);

    List<UserContext> getUserContexts(String userId);

    UserContext getUserContext(String userId, String userContextId);

    UserContext updateUserContext(String userId, UserContext userContext);

    void deleteUserContext(String userId, String userContextId);


}
