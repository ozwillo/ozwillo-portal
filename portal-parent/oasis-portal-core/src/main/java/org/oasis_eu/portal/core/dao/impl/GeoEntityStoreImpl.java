package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.model.appstore.GeoEntity;

/**
 * User: schambon
 * Date: 5/30/14
 */
public class GeoEntityStoreImpl extends DatacoreBasedCRUDStoreImpl<GeoEntity> {
    @Override
    protected String getTypeName() {
        return "geographic-entity";
    }
}
