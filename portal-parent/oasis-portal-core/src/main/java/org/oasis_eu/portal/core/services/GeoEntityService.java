package org.oasis_eu.portal.core.services;

import org.oasis_eu.portal.core.model.appstore.GeoEntity;

import java.util.List;
import java.util.Set;

/**
 * User: schambon
 * Date: 6/16/14
 */
public interface GeoEntityService {

    Set<String> getAllEntityNames();

    Set<GeoEntity> getEntitiesByName(String name);

    GeoEntity find(String id);

    Set<GeoEntity> getAllSuperEntities(GeoEntity root);

}
