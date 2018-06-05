package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;

import java.util.List;

/**
 * User: schambon
 * Date: 8/8/14
 */
public interface ApplicationInstanceStore {

    List<ApplicationInstance> findByUserId(String userId, boolean includeOrgs);

    List<ApplicationInstance> findByOrganizationId(String organizationId);

    List<ApplicationInstance> findPendingInstances(String userId);
}
