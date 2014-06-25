package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.OrganizationStore;
import org.oasis_eu.portal.core.model.appstore.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * User: schambon
 * Date: 6/25/14
 */
@Service
public class OrganizationStoreImpl implements OrganizationStore {

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Value("${kernel.user_directory_endpoint}")
    private String endpoint;

    @Override
    public Organization find(String id) {
        return kernelRestTemplate.getForObject(endpoint + "/org/{id}", Organization.class, id);
    }
}
