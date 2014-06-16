package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.appstore.LocalService;

import java.util.List;

/**
 * User: schambon
 * Date: 6/13/14
 */
public interface LocalServiceStore {

    LocalService find(String localServiceId);

    List<LocalService> findByTerritory(List<String> territoryIds);
}
