package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstantiationRequest;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.Audience;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/24/14
 */
@Service
public class CatalogStoreImpl implements CatalogStore {

    private static final Logger logger = LoggerFactory.getLogger(CatalogStoreImpl.class);

    @Autowired
    private RestTemplate kernelRestTemplate;

    @Value("${kernel.portal_endpoints.catalog:''}")
    private String endpoint;

    @Value("${kernel.portal_endpoints.apps:''}")
    private String appsEndpoint;



    @Override
    public List<CatalogEntry> findAllVisible(List<Audience> targetAudiences) {
        URI uri = UriComponentsBuilder.fromHttpUrl(endpoint)
                .path("/search")
//                .queryParam("targetAudience", (Object[]) targetAudiences.toArray(new Audience[targetAudiences.size()]))
                .build()
                .toUri();

        return Arrays.asList(kernelRestTemplate.getForObject(uri, CatalogEntry[].class)).stream().filter(e -> targetAudiences.contains(e.getTargetAudience())).collect(Collectors.toList());
    }

    @Override
    public CatalogEntry findApplication(String id) {
        return getCatalogEntry(id, appsEndpoint + "/app/{id}");
    }


    @Override
    public CatalogEntry findService(String id) {
        return getCatalogEntry(id, appsEndpoint + "/service/{id}");
    }

    private CatalogEntry getCatalogEntry(String id, String endpoint) {
        ResponseEntity<CatalogEntry> response = kernelRestTemplate.getForEntity(endpoint, CatalogEntry.class, id);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else if (response.getStatusCode().is4xxClientError()) {
            logger.warn("Cannot find catalog entry {} through endpoint {}", id, endpoint);
            return null;
        } else {
            throw new HttpClientErrorException(response.getStatusCode(), response.getStatusCode().getReasonPhrase());
        }

    }

    @Override
    public void instantiate(String appId, ApplicationInstantiationRequest instancePattern) {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication instanceof OpenIdCAuthentication) {
            headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) authentication).getAccessToken()));
        }


        HttpEntity<ApplicationInstantiationRequest> request = new HttpEntity<>(instancePattern, headers);

        ResponseEntity<String> result = kernelRestTemplate.postForEntity(endpoint + "/instantiate/{appId}", request, String.class, appId);
        result.getHeaders().entrySet().stream().forEach(e -> logger.debug("{}: {}", e.getKey(), e.getValue()));
        logger.debug(result.getBody());

    }

    @Override
    public List<CatalogEntry> findServicesOfInstance(String instanceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) SecurityContextHolder.getContext().getAuthentication()).getAccessToken()));

        return Arrays.asList(kernelRestTemplate.exchange(appsEndpoint + "/instance/{instance_id}/services", HttpMethod.GET, new HttpEntity<Object>(headers), CatalogEntry[].class, instanceId).getBody());

    }

    @Override
    public ApplicationInstance findApplicationInstance(String instanceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) SecurityContextHolder.getContext().getAuthentication()).getAccessToken()));

        return kernelRestTemplate.exchange(appsEndpoint + "/instance/{instance_id}", HttpMethod.GET, new HttpEntity<Object>(headers), ApplicationInstance.class, instanceId).getBody();

    }
}
