package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.appstore.Organization;

/**
 * User: schambon
 * Date: 6/25/14
 */
public interface OrganizationStore {

    Organization find(String id);

}
