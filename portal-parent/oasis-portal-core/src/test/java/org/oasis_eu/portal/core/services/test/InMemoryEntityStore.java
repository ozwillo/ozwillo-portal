package org.oasis_eu.portal.core.services.test;

import org.oasis_eu.portal.core.dao.GenericCRUDStore;
import org.oasis_eu.portal.core.exception.EntityNotFoundException;
import org.oasis_eu.portal.core.exception.InvalidEntityException;
import org.oasis_eu.portal.core.model.appstore.GenericEntity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 5/28/14
 */
public class InMemoryEntityStore<T extends GenericEntity> implements GenericCRUDStore<T> {

    LinkedList<T> entities = new LinkedList<>();

    @Override
    public T create(T t) {
        if (t.getId() != null) {
            throw new InvalidEntityException("Entity already has an id: " + t.getId());
        }
        t.setId(UUID.randomUUID().toString());
        entities.add(t);
        return t;
    }

    /**
     * Useful for testing: lets the cat id be externally controlled
     * @param t
     */
    public void addEntity(T t) {
        entities.add(t);
    }

    @Override
    public void update(T t) {
        if (!entities.contains(t)) {
            throw new EntityNotFoundException(t.getId());
        }
        // nop, we work by reference
    }

    @Override
    public void delete(T t) {
        if (!entities.contains(t)) {
            throw new EntityNotFoundException(t.getId());
        }
        entities.removeIf(cat -> cat.getId().equals(t.getId()));
    }

    @Override
    public int count() {
        return entities.size();
    }

    @Override
    public T find(String id) {
        return entities.stream().filter(cat -> cat.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<T> find() {
        return new ArrayList<>(entities);
    }

    @Override
    public List<T> find(int skip, int number) {
        List<T> collected = entities.stream().skip(skip).collect(Collectors.toList());
        if (collected.size() > number) {
            collected = collected.subList(0, number);
        }

        return collected;
    }

    @Override
    public void moveBefore(T t1, T t2) {
        T subject = find(t1.getId());
        if (subject == null) {
            throw new EntityNotFoundException(t1.getId());
        }
        T object = find(t2.getId());
        if (object == null) {
            throw new EntityNotFoundException(t2.getId());
        }

        entities.remove(subject);
        entities.add(entities.indexOf(object), subject);
    }

    @Override
    public void pushToEnd(T t) {
        if (! entities.contains(t)) {
            throw new EntityNotFoundException(t.getId());
        }
        entities.remove(t);
        entities.add(t);
    }
}
