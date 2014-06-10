package org.oasis_eu.portal.core.services.impl;

import org.oasis_eu.portal.core.dao.GenericCRUDStore;
import org.oasis_eu.portal.core.model.GenericEntity;
import org.oasis_eu.portal.core.services.GenericCRUDService;

import java.util.List;

/**
 * User: schambon
 * Date: 5/30/14
 */
public class GenericCRUDServiceImpl<T extends GenericEntity> implements GenericCRUDService<T> {
    protected GenericCRUDStore<T> store;

    public GenericCRUDServiceImpl(GenericCRUDStore<T> store) {
        this.store = store;
    }

    @Override
    public void moveBefore(T subject, T object) {
        store.moveBefore(subject, object);
    }

    @Override
    public void pushToEnd(T subject) {
        store.pushToEnd(subject);
    }

    @Override
    public T create(T t) {
        return store.create(t);
    }

    @Override
    public void update(T t) {
        store.update(t);
    }

    @Override
    public T find(String id) {
        return store.find(id);
    }

    @Override
    public List<T> find() {
        return store.find();
    }

    @Override
    public List<T> find(int skip, int limit) {
        return store.find(skip, limit);
    }

    @Override
    public void delete(T t) {
        store.delete(t);
    }
}
