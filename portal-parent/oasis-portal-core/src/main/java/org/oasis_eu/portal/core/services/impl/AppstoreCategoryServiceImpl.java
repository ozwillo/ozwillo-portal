package org.oasis_eu.portal.core.services.impl;

import org.oasis_eu.portal.core.constants.PortalConstants;
import org.oasis_eu.portal.core.dao.AppstoreCategoryStore;
import org.oasis_eu.portal.core.exception.InvalidEntityException;
import org.oasis_eu.portal.core.model.AppstoreCategory;
import org.oasis_eu.portal.core.services.AppstoreCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: schambon
 * Date: 5/30/14
 */
@Service
public class AppstoreCategoryServiceImpl extends GenericCRUDServiceImpl<AppstoreCategory> implements AppstoreCategoryService {

    @Autowired
    public AppstoreCategoryServiceImpl(AppstoreCategoryStore store) {
        super(store);
    }

    @Override
    public AppstoreCategory create(AppstoreCategory appstoreCategory) {
        if (appstoreCategory.getNames().size() == 0) {
            throw new InvalidEntityException("Illegal creation of nameless appstore category");
        }

        if (appstoreCategory.getLocalizedName(PortalConstants.PORTAL_DEFAULT_LOCALE) == null) {
            throw new InvalidEntityException("Illegal creation of appstore category with no name in the fallback portal language");
        }

        return super.create(appstoreCategory);
    }
}
