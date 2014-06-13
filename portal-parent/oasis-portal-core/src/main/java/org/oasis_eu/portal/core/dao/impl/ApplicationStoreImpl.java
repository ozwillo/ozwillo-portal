package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.ApplicationStore;
import org.oasis_eu.portal.core.model.appstore.Application;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * User: schambon
 * Date: 5/30/14
 */
@Component
public class ApplicationStoreImpl implements ApplicationStore {
    @Override
    public Application create(Application application) {
        return null;
    }

    @Override
    public void update(Application application) {

    }

    @Override
    public void delete(Application application) {

    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public Application find(String id) {
        return null;
    }

    @Override
    public List<Application> find() {
        return null;
    }

    @Override
    public List<Application> find(int skip, int number) {
        return null;
    }

}
