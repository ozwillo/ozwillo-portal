package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.ApplicationInstanceStore;
import org.oasis_eu.portal.core.exception.EntityNotFoundException;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

/**
 * User: schambon
 * Date: 8/8/14
 */
@Service
public class ApplicationInstanceStoreImpl implements ApplicationInstanceStore {

    @Autowired
    private Kernel kernel;

    @Value("${kernel.portal_endpoints.apps:''}")
    private String appsEndpoint;

    @Override
    public List<ApplicationInstance> findByUserId(String userId) {

        return Arrays.asList(kernel.exchange(appsEndpoint + "/instance/user/{user_id}", HttpMethod.GET, null, ApplicationInstance[].class, user(), userId).getBody());

    }

    public List<ApplicationInstance> findByOrganizationId(String organizationId) {

        ResponseEntity<ApplicationInstance[]> exchange = kernel.exchange(appsEndpoint + "/instance/organization/{organization_id}", HttpMethod.GET, null, ApplicationInstance[].class, user(), organizationId);

        // note: we throw exceptions corresponding to what the actual result is. The response message itself is already logged so we are not losing any information.
        if (exchange.getStatusCode().is2xxSuccessful() ) {
            return Arrays.asList(exchange.getBody());
        } else if (HttpStatus.NOT_FOUND.equals(exchange.getStatusCode())) {
            throw new EntityNotFoundException();
        } else if (exchange.getStatusCode().is4xxClientError()) {
            throw new WrongQueryException();
        } else {
            throw new TechnicalErrorException();
        }

    }

}
