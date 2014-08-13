package org.oasis_eu.portal.core.dao.impl;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.oasis_eu.portal.core.constants.OasisLocales;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstantiationRequest;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.Audience;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.catalog.PaymentOption;
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
import java.util.*;
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

        CatalogEntry[] body = kernelRestTemplate.exchange(appsEndpoint + "/instance/{instance_id}/services", HttpMethod.GET, new HttpEntity<Object>(headers), CatalogEntry[].class, instanceId).getBody();
        if (body != null) {
            return Arrays.asList(body);
        } else {
            logger.error("Empty services collection found for instance {}", instanceId);
            return Collections.emptyList();
        }

    }

    @Override
    public ApplicationInstance findApplicationInstance(String instanceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) SecurityContextHolder.getContext().getAuthentication()).getAccessToken()));

        return kernelRestTemplate.exchange(appsEndpoint + "/instance/{instance_id}", HttpMethod.GET, new HttpEntity<Object>(headers), ApplicationInstance.class, instanceId).getBody();

    }


    @Override
    public CatalogEntry fetchAndUpdateService(String serviceId, CatalogEntry service) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", ((OpenIdCAuthentication) SecurityContextHolder.getContext().getAuthentication()).getAccessToken()));

        // we need to be sure to grab everything from the original

        ResponseEntity<KernelService> entity = kernelRestTemplate.exchange(appsEndpoint + "/service/{service_id}", HttpMethod.GET, new HttpEntity<Object>(headers), KernelService.class, serviceId);
        String etag = entity.getHeaders().get("ETag").get(0);

        KernelService kernelService = entity.getBody();
        kernelService.name = service.getDefaultName();
        kernelService.description = service.getDefaultDescription();

        // kinda hacky, but we'll change that when the UI fully supports l10n
        for (Locale locale : OasisLocales.values()) {
            kernelService.set("name#" + locale.getLanguage(), service.getDefaultName());
            kernelService.set("description#" + locale.getLanguage(), service.getDefaultDescription());
        }

        kernelService.territory_id = service.getTerritoryId();
        kernelService.visible = service.isVisible();

        headers.add("If-Match", etag);

        return kernelRestTemplate.exchange(appsEndpoint + "/service/{service_id}", HttpMethod.PUT, new HttpEntity<>(kernelService, headers), CatalogEntry.class, serviceId).getBody();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class KernelService {

        // blacklist of properties we want forgotten and never sent back to the kernel
        private static final Set<String> BLACKLISTED = new HashSet<String>() {{
            add("modified");
            add("type");
        }};

        String id;
        String name;
        String description;
        String icon;
        String provider_id;
        PaymentOption paymentOption;
        Audience audience;
        List<String> category_ids;
        boolean visible;
        String local_id;
        String instance_id;
        String service_uri;
        String notification_uri;
        List<String> redirect_uris;
        List<String> post_logout_redirect_uris;
        String territory_id;
        String subscription_id;
        String subscription_secret;

        @JsonIgnore
        private Map<String, String> otherProperties = new HashMap<>();

        @JsonAnySetter
        public void set(String key, String value) {
            if (! BLACKLISTED.contains(key)) {
                otherProperties.put(key, value);
            }
        }

        @JsonAnyGetter
        public Map<String, String> get() {
            return otherProperties;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getProvider_id() {
            return provider_id;
        }

        public void setProvider_id(String provider_id) {
            this.provider_id = provider_id;
        }

        public PaymentOption getPaymentOption() {
            return paymentOption;
        }

        public void setPaymentOption(PaymentOption paymentOption) {
            this.paymentOption = paymentOption;
        }

        public Audience getAudience() {
            return audience;
        }

        public void setAudience(Audience audience) {
            this.audience = audience;
        }

        public List<String> getCategory_ids() {
            return category_ids;
        }

        public void setCategory_ids(List<String> category_ids) {
            this.category_ids = category_ids;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public String getLocal_id() {
            return local_id;
        }

        public void setLocal_id(String local_id) {
            this.local_id = local_id;
        }

        public String getInstance_id() {
            return instance_id;
        }

        public void setInstance_id(String instance_id) {
            this.instance_id = instance_id;
        }

        public String getService_uri() {
            return service_uri;
        }

        public void setService_uri(String service_uri) {
            this.service_uri = service_uri;
        }

        public String getNotification_uri() {
            return notification_uri;
        }

        public void setNotification_uri(String notification_uri) {
            this.notification_uri = notification_uri;
        }

        public List<String> getRedirect_uris() {
            return redirect_uris;
        }

        public void setRedirect_uris(List<String> redirect_uris) {
            this.redirect_uris = redirect_uris;
        }

        public List<String> getPost_logout_redirect_uris() {
            return post_logout_redirect_uris;
        }

        public void setPost_logout_redirect_uris(List<String> post_logout_redirect_uris) {
            this.post_logout_redirect_uris = post_logout_redirect_uris;
        }

        public String getTerritory_id() {
            return territory_id;
        }

        public void setTerritory_id(String territory_id) {
            this.territory_id = territory_id;
        }

        public String getSubscription_id() {
            return subscription_id;
        }

        public void setSubscription_id(String subscription_id) {
            this.subscription_id = subscription_id;
        }

        public String getSubscription_secret() {
            return subscription_secret;
        }

        public void setSubscription_secret(String subscription_secret) {
            this.subscription_secret = subscription_secret;
        }
    }
}
