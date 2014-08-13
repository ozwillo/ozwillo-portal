package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.ApplicationInstanceStore;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * User: schambon
 * Date: 8/8/14
 */
@Service
public class ApplicationInstanceStoreImpl implements ApplicationInstanceStore {

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Value("${kernel.portal_endpoints.apps:''}")
    private String appsEndpoint;

    @Override
    public List<ApplicationInstance> findByUserId(String userId) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) SecurityContextHolder.getContext().getAuthentication()).getAccessToken()));

        ResponseEntity<ApplicationInstance[]> exchange = kernelRestTemplate.exchange(appsEndpoint + "/instance/user/{user_id}", HttpMethod.GET, new HttpEntity<>(headers), ApplicationInstance[].class, userId);
        return Arrays.asList(exchange.getBody());

    }

    public List<ApplicationInstance> findByOrganizationId(String organizationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) SecurityContextHolder.getContext().getAuthentication()).getAccessToken()));

        ResponseEntity<ApplicationInstance[]> exchange = kernelRestTemplate.exchange(appsEndpoint + "/instance/organization/{organization_id}", HttpMethod.GET, new HttpEntity<Object>(headers), ApplicationInstance[].class, organizationId);
        return Arrays.asList(exchange.getBody());
    }

}
