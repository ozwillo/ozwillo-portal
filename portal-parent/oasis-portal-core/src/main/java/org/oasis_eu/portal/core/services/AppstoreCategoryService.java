package org.oasis_eu.portal.core.services;

import org.oasis_eu.portal.core.model.AppstoreCategory;

import java.util.List;

/**
 * User: schambon
 * Date: 5/28/14
 */
public interface AppstoreCategoryService extends GenericCRUDService<AppstoreCategory> {

    void moveBefore(AppstoreCategory subject, AppstoreCategory object);

    void pushToEnd(AppstoreCategory subject);

}
