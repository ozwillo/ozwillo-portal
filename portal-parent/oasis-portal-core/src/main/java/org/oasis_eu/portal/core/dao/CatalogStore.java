package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.appstore.AppInstance;
import org.oasis_eu.portal.core.model.appstore.Audience;
import org.oasis_eu.portal.core.model.appstore.CatalogEntry;
import org.oasis_eu.spring.kernel.model.instance.CreateInstanceRequest;

import java.util.List;

/**
 * User: schambon
 * Date: 6/24/14
 */
public interface CatalogStore {

    CatalogEntry find(String id);

    List<CatalogEntry> findAllVisible(List<Audience> targetAudience);

    void instantiate(String appId, AppInstance instancePattern);
}
