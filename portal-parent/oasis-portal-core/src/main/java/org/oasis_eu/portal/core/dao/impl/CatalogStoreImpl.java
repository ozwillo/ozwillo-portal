package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.appstore.Audience;
import org.oasis_eu.portal.core.model.appstore.CatalogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * User: schambon
 * Date: 6/24/14
 */
@Service
public class CatalogStoreImpl implements CatalogStore {

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Value("${kernel.portal_endpoints.catalog}")
    private String endpoint;

    @Override
    public List<CatalogEntry> findAllVisible(List<Audience> targetAudiences) {
        URI uri = UriComponentsBuilder.fromHttpUrl(endpoint)
                .queryParam("targetAudience", targetAudiences.toArray(new Audience[targetAudiences.size()]))
                .build()
                .toUri();

        return Arrays.asList(kernelRestTemplate.getForObject(uri, CatalogEntry[].class));
    }

    @Override
    public CatalogEntry find(String id) {
        return kernelRestTemplate.getForObject(endpoint + "/{id}", CatalogEntry.class, id);
    }

    @Override
    public void subscribe(String appId, String userId) {
        kernelRestTemplate.postForObject(endpoint + "/buy/{appId}/{userId}", "", CatalogEntry.class, appId, userId);
    }
}
