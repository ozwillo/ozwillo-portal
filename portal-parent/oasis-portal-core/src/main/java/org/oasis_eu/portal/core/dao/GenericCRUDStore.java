package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.appstore.GenericEntity;

import java.util.List;

/**
 * User: schambon
 * Date: 5/30/14
 */
public interface GenericCRUDStore<T extends GenericEntity> {

    /**
     * Save the entity
     * @param t the entity to create
     * @return the entity with updated id
     * @throws org.oasis_eu.portal.core.exception.InvalidEntityException if there is already an id to this entity
     */
    T create(T t);

    /**
     * Update an entity
     * @param t the entity to update
     * @throws org.oasis_eu.portal.core.exception.EntityNotFoundException if it can't be found
     */
    void update(T t);

    /**
     * Delete an entity
     * @param t the entity to delete
     * @throws org.oasis_eu.portal.core.exception.EntityNotFoundException if it can't be found
     */
    void delete(T t);

    /**
     * how many entities are there?
     * @return the total number of entities
     */
    int count();

    /**
     * Get an entity by id
     * @param id    the id of the entity to find
     * @return the entity, or null if it can't be found
     */
    T find(String id);

    /**
     * List all entities
     * @return the list of all entities.
     */
    List<T> find();

    /**
     * List a maximum of <code>number</code> entities, skipping <code>skip</code>
     * @param skip
     * @param number
     * @return a list of entities, guaranteed to contain at most <code>number</code> items
     */
    List<T> find(int skip, int number);

    void pushToEnd(T t);

    void moveBefore(T t1, T t2);
}
