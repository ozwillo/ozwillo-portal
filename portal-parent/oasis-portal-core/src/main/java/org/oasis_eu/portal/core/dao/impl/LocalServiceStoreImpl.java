package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.LocalServiceStore;
import org.oasis_eu.portal.core.model.appstore.LocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

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

    @Override
    public List<LocalService> findByTerritory(List<String> territoryIds) {

        URI uri = UriComponentsBuilder.fromUriString(endpoint)
                .path("/service")
                .queryParam("territories", territoryIds.toArray())
                .build()
                .toUri();

        return Arrays.asList(kernelRestTemplate.getForObject(uri, LocalService[].class));
    }
}
