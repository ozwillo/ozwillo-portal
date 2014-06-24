package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.ApplicationStore;
import org.oasis_eu.portal.core.dao.LocalServiceStore;
import org.oasis_eu.portal.core.model.appstore.GeoEntity;
import org.oasis_eu.portal.core.model.appstore.LocalService;
import org.oasis_eu.portal.core.services.GeoEntityService;
import org.oasis_eu.spring.kernel.model.Address;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/16/14
 */
@Service
public class LocalServiceService {

    @Autowired
    private GeoEntityService geoEntityService;

    @Autowired
    private LocalServiceStore localServiceStore;

    @Autowired
    private UserInfoHelper userInfoHelper;

    public List<LocalService> findLocalServices() {

        Address address = userInfoHelper.currentUser().getAddress();
        if (address == null || address.getLocality() == null) {
            return Collections.emptyList();
        }
        Set<GeoEntity> entities = geoEntityService.getEntitiesByName(address.getLocality());
        entities.addAll(geoEntityService.getEntitiesByName(address.getPostalCode()));

        List<String> closure = entities.stream().flatMap(e -> geoEntityService.getAllSuperEntities(e).stream().map(s -> s.getId())).collect(Collectors.toList());

        return localServiceStore.findByTerritory(closure);
    }

}
