package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.LocalServiceStore;
import org.oasis_eu.portal.core.model.appstore.LocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Component
public class LocalServiceStoreImpl implements LocalServiceStore {

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Value("${kernel.portal_endpoints.appstore}")
    private String endpoint;


    @Override
    public LocalService find(String id) {
        return kernelRestTemplate.getForObject(endpoint + "/service/{service_id}", LocalService.class, id);
    }

}
