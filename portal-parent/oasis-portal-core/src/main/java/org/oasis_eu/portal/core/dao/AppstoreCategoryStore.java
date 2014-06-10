package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.AppstoreCategory;

/**
 * User: schambon
 * Date: 5/28/14
 */
public interface AppstoreCategoryStore extends GenericCRUDStore<AppstoreCategory> {


    /**
     * Move a category
     * @param categoryOne  the category to move (or subject)
     * @param categoryTwo  the category before which to move the subject category
     * @throws org.oasis_eu.portal.core.exception.EntityNotFoundException if either category is not found
     */
    void moveBefore(AppstoreCategory categoryOne, AppstoreCategory categoryTwo);

    /**
     * Move a category to the end of the list
     * @param category the category to move
     * @throws org.oasis_eu.portal.core.exception.EntityNotFoundException if the category doesn't exist
     */
    void pushToEnd(AppstoreCategory category);
}
