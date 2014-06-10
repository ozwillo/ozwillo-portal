package org.oasis_eu.portal.core.services;

import org.oasis_eu.portal.core.model.*;

import java.util.List;

/**
 * User: schambon
 * Date: 5/14/14
 */
public interface ApplicationService extends GenericCRUDService<Application> {


    List<AppStoreHit> search(String fullText, SearchControls controls);

}
