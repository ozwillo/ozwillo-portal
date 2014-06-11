package org.oasis_eu.portal.core.services.impl;

import org.oasis_eu.portal.core.dao.ApplicationStore;
import org.oasis_eu.portal.core.dao.AppstoreCategoryStore;
import org.oasis_eu.portal.core.dao.GeoEntityStore;
import org.oasis_eu.portal.core.model.AppStoreHit;
import org.oasis_eu.portal.core.model.Application;
import org.oasis_eu.portal.core.model.AppstoreCategory;
import org.oasis_eu.portal.core.model.SearchControls;
import org.oasis_eu.portal.core.services.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User: schambon
 * Date: 5/30/14
 */
//@Service
public class ApplicationServiceImpl extends GenericCRUDServiceImpl<Application> implements ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Autowired
    private AppstoreCategoryStore categoryStore;

    @Autowired
    private GeoEntityStore geoEntityStore;

    @Autowired
    public ApplicationServiceImpl(ApplicationStore applicationStore) {
        super(applicationStore);
    }

    @Override
    public Application find(String id) {
        Application application = super.find(id);
        setApplicationCategory(application);

        return application;
    }

    @Override
    public List<Application> find() {

        List<Application> list = super.find();
        list.forEach(app -> setApplicationCategory(app));
        return list;
    }

    @Override
    public List<Application> find(int skip, int limit) {
        List<Application> list = super.find(skip, limit);
        list.forEach(app -> setApplicationCategory(app));
        return list;
    }

    @Override
    public List<AppStoreHit> search(String fullText, SearchControls controls) {
        return null;
    }

    private void setApplicationCategory(Application application) {
        application.getCategoryIds().forEach(id -> {
            AppstoreCategory category = categoryStore.find(id);
            if (category != null) {
                application.getCategories().add(category);
            } else {
                logger.warn("Application {} references non-existing category id {}", application.getId(), id);
            }
        });
    }
}
