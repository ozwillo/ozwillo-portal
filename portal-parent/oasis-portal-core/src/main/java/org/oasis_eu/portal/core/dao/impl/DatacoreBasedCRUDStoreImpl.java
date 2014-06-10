package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.GenericCRUDStore;
import org.oasis_eu.portal.core.model.GenericEntity;

import java.util.List;

/**
 * TODO implement meeee!
 * User: schambon
 * Date: 5/30/14
 */
abstract public class DatacoreBasedCRUDStoreImpl<T extends GenericEntity> implements GenericCRUDStore<T> {

    abstract protected String getTypeName();

    @Override
    public T create(T t) {
        return null;
    }

    @Override
    public void update(T t) {

    }

    @Override
    public void delete(T t) {

    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public T find(String id) {
        return null;
    }

    @Override
    public List<T> find() {
        return null;
    }

    @Override
    public List<T> find(int skip, int number) {
        return null;
    }

    @Override
    public void pushToEnd(T t) {

    }

    @Override
    public void moveBefore(T t1, T t2) {

    }
}
