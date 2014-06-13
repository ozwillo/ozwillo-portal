package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.appstore.GenericEntity;

/**
 * User: schambon
 * Date: 6/13/14
 */
public interface OrderedCRUDStore<T extends GenericEntity> extends GenericCRUDStore<T> {
    void pushToEnd(T t);

    void moveBefore(T t1, T t2);
}
