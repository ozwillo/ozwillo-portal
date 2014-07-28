package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.appstore.AppInstance;
import org.oasis_eu.portal.core.model.appstore.Audience;
import org.oasis_eu.portal.core.model.appstore.CatalogEntry;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

    @Value("${kernel.portal_endpoints.catalog}")
    private String endpoint;

    @Value("${kernel.portal_endpoints.apps}")
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
    public CatalogEntry find(String id) {
        return kernelRestTemplate.getForObject(appsEndpoint + "/app/{id}", CatalogEntry.class, id);
    }

    @Override
    public void instantiate(String appId, AppInstance instancePattern) {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication instanceof OpenIdCAuthentication) {
            headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) authentication).getAccessToken()));
        }


        HttpEntity<AppInstance> request = new HttpEntity<>(instancePattern, headers);

        ResponseEntity<String> result = kernelRestTemplate.postForEntity(endpoint + "/instantiate/{appId}", request, String.class, appId);
        result.getHeaders().entrySet().stream().forEach(e -> logger.debug("{}: {}", e.getKey(), e.getValue()));
        logger.debug(result.getBody());

    }
}
