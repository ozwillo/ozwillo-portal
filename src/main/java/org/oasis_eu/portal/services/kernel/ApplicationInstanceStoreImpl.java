package org.oasis_eu.portal.services.kernel;

import org.oasis_eu.portal.model.kernel.instance.ApplicationInstance;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;

/**
 * User: schambon
 * Date: 8/8/14
 */
@Service
public class ApplicationInstanceStoreImpl {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationInstanceStoreImpl.class);

    @Autowired
    private Kernel kernel;

    @Value("${kernel.portal_endpoints.apps:}")
    private String appsEndpoint;

    @Cacheable("user-instances")
    public List<ApplicationInstance> findByUserId(String userId, boolean include_orgs) {
        return Arrays.asList(kernel.getEntityOrException(appsEndpoint + "/instance/user/{user_id}?include_orgs={include_orgs}",
            ApplicationInstance[].class, user(), userId, include_orgs));

    }

    @Cacheable("org-instances")
    public List<ApplicationInstance> findByOrganizationId(String organizationId) {
        ApplicationInstance[] appInstanceArray = kernel.getEntityOrException(appsEndpoint + "/instance/organization/{organization_id}",
            ApplicationInstance[].class, user(), organizationId);
        return Arrays.asList(appInstanceArray);
    }

    // NOT cacheable - we want to call the kernel all the time for that
    public List<ApplicationInstance> findPendingInstances(String userId) {
        String uriString = UriComponentsBuilder.fromHttpUrl(appsEndpoint)
            .path("/instance/user/{user_id}")
            .queryParam("include_orgs", true)
            .queryParam("status", "PENDING")
            .build()
            .expand(userId)
            .toUriString();

        List<ApplicationInstance> appInstancesList = Arrays.asList(kernel.getEntityOrException(uriString, ApplicationInstance[].class, user()));
        logger.debug("Found {} pending instances", appInstancesList.size());
        return appInstancesList;
    }


    public ApplicationInstance deletePendingInstance(String instanceId){
        ResponseEntity<ApplicationInstance> respAppInstance = kernel.exchange(appsEndpoint + "/instance/{instance_id}",
                HttpMethod.GET, null, ApplicationInstance.class, user(), instanceId);
        String eTag = respAppInstance.getHeaders().getETag();

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", eTag);

        ResponseEntity<ApplicationInstance> object = kernel.exchange(appsEndpoint + "/instance/{instanceId}", HttpMethod.DELETE,
                new HttpEntity<>(null,headers), ApplicationInstance.class, user(), instanceId);
        logger.debug("Delete pending instance", object);
        return object.getBody();
    }
}
