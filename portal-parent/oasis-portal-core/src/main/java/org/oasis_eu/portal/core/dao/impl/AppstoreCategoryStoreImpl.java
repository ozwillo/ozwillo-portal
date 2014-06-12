package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.AppstoreCategoryStore;
import org.oasis_eu.portal.core.model.appstore.AppstoreCategory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User: schambon
 * Date: 5/30/14
 */
@Repository
public class AppstoreCategoryStoreImpl implements AppstoreCategoryStore {
    @Override
    public List<AppstoreCategory> find(int skip, int number) {
        return null;
    }

    @Override
    public List<AppstoreCategory> find() {
        return null;
    }

    @Override
    public AppstoreCategory find(String id) {
        return null;
    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public void delete(AppstoreCategory appstoreCategory) {

    }

    @Override
    public void update(AppstoreCategory appstoreCategory) {

    }

    @Override
    public AppstoreCategory create(AppstoreCategory appstoreCategory) {
        return null;
    }

    @Override
    public void pushToEnd(AppstoreCategory category) {

    }

    @Override
    public void moveBefore(AppstoreCategory categoryOne, AppstoreCategory categoryTwo) {

    }
}
