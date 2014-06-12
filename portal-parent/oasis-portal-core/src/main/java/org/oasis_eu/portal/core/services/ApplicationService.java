package org.oasis_eu.portal.core.services;

import org.oasis_eu.portal.core.model.appstore.AppStoreHit;
import org.oasis_eu.portal.core.model.appstore.Application;
import org.oasis_eu.portal.core.model.appstore.SearchControls;

import java.util.List;

/**
 * User: schambon
 * Date: 5/14/14
 */
public interface ApplicationService extends GenericCRUDService<Application> {


    List<AppStoreHit> search(String fullText, SearchControls controls);

}
