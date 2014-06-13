package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.ApplicationStore;
import org.oasis_eu.portal.core.model.appstore.Application;
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
 * Date: 5/30/14
 */
@Component
public class ApplicationStoreImpl implements ApplicationStore {

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Value("${kernel.portal_endpoints.appstore}")
    private String endpoint;


    @Override
    public Application create(Application application) {
        return null;
    }

    @Override
    public void update(Application application) {

    }

    @Override
    public void delete(Application application) {

    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public Application find(String id) {
        return kernelRestTemplate.getForObject(endpoint + "/app/{application_id}", Application.class, id);
    }

    @Override
    public List<Application> find() {
        return find(0, 25);
    }

    @Override
    public List<Application> find(int skip, int number) {
        URI uri = UriComponentsBuilder.fromHttpUrl(endpoint)
                .path("/app")
                .queryParam("start", skip)
                .queryParam("limit", number)
                .build()
                .toUri();

        return Arrays.asList(kernelRestTemplate.getForObject(uri, Application[].class));
    }

}
