package org.oasis_eu.portal.core.dao.impl;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.none;
import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.user;
import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.userIfExists;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.core.constants.OasisLocales;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstanceCreationException;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstantiationRequest;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance.InstantiationStatus;
import org.oasis_eu.portal.core.model.catalog.Audience;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.core.model.catalog.PaymentOption;
import org.oasis_eu.portal.core.mongo.dao.store.InstalledStatusRepository;
import org.oasis_eu.portal.core.mongo.model.store.InstalledStatus;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon
 * Date: 6/24/14
 */
@Service
public class CatalogStoreImpl implements CatalogStore {

    private static final Logger logger = LoggerFactory.getLogger(CatalogStoreImpl.class);

    @Autowired
    private Kernel kernel;

    @Value("${kernel.portal_endpoints.catalog:}")
    private String endpoint;

    @Value("${kernel.portal_endpoints.apps:}")
    private String appsEndpoint;

    @Value("${application.store.load_size:20}")
    private int loadSize;
    
    /** to get locale */
    @Autowired
    private HttpServletRequest request;

    // a couple of MongoDB-backed caches
    @Autowired
    private InstalledStatusRepository installedStatusRepository;

    @Autowired
    private UserInfoService userInfoHelper;


    @Override
    @Cacheable("appstore")
    public List<CatalogEntry> findAllVisible(List<Audience> targetAudiences, List<PaymentOption> paymentOptions,
            List<Locale> supportedLocales, List<String> geographicalAreas,
            List<String> categoryIds, String q, String hl, int from) {
        // NB. see Kernel API /m/search http://kernel.ozwillo-preprod.eu/swagger-ui/#!/market-search/get
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(endpoint)
                .path("/search")
                .queryParam("start", from)
                .queryParam("limit", loadSize) // *4 if there is any remaining post get, portal-side filter
                ;
        if (hl != null && !hl.isEmpty()) {
            uriBuilder.queryParam("hl", hl);
        }
        targetAudiences.stream().forEach(audience -> uriBuilder.queryParam("target_audience", audience));
        paymentOptions.stream().forEach(paymentOption -> uriBuilder.queryParam("payment_option", paymentOption));
        if (supportedLocales != null) supportedLocales.stream().forEach(supportedLocale -> uriBuilder.queryParam("supported_locale", supportedLocale.getLanguage()));
        if (geographicalAreas != null) geographicalAreas.stream().forEach(geographicalArea -> uriBuilder.queryParam("geographical_areas", geographicalArea));
        //if (restrictedAreas != null) restrictedAreas.stream().forEach(restrictedArea -> uriBuilder.queryParam("restricted_areas", restrictedArea)); // TODO from user locale !?
        if (categoryIds != null) categoryIds.stream().forEach(categoryId -> uriBuilder.queryParam("category_id", categoryId));
        if (q != null && !q.isEmpty()) {
            uriBuilder.queryParam("q", q);
        }
        
        String uri = uriBuilder.build().toUriString();

        List<CatalogEntry> catalogEntries = Arrays.asList(kernel.getForObject(uri, CatalogEntry[].class, none()));

        if (logger.isDebugEnabled()) {
            logger.debug("Found catalog entries:");
            catalogEntries.forEach(e -> logger.debug(e.toString()));
        }

        return catalogEntries
                .stream()
                //.filter(e -> e.getTargetAudience().stream().anyMatch(audience -> targetAudiences.contains(audience))) // done on Kernel side
                //.filter(e -> paymentOptions.stream().anyMatch(option -> option.equals(e.getPaymentOption()))) // done on Kernel side
                //.limit(loadSize) // if there is any remaining post get, portal-side filter
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable("applications")
    public CatalogEntry findApplication(String id) {
        return getCatalogEntry(id, appsEndpoint + "/app/{id}");
    }


    @Cacheable("services")
    public CatalogEntry findService(String id) {
        return getCatalogEntry(id, appsEndpoint + "/service/{id}");
    }

    private CatalogEntry getCatalogEntry(String id, String endpoint) {

        return kernel.getEntityOrNull(endpoint, CatalogEntry.class, userIfExists(), id);
    }

    @Override
    public void instantiate(String appId, ApplicationInstantiationRequest instancePattern) {

        logger.info("Application instantiation request: {}", instancePattern);

        InstalledStatus status = installedStatusRepository.findByCatalogEntryTypeAndCatalogEntryIdAndUserId(CatalogEntryType.APPLICATION, appId, userInfoHelper.currentUser().getUserId());
        if (status != null) {
            installedStatusRepository.delete(status);
        }

        try {
            ResponseEntity<String> responseEntity = kernel.exchange(endpoint + "/instantiate/{appId}", HttpMethod.POST, new HttpEntity<ApplicationInstantiationRequest>(instancePattern), String.class, user(), appId);
            if (responseEntity.getStatusCode().is4xxClientError()) {
                logger.error("Got a client error when creating an instance of application {} ({}): {}", appId, instancePattern.getName(), responseEntity.getStatusCode().getReasonPhrase());
                throw new ApplicationInstanceCreationException(appId, instancePattern, ApplicationInstanceCreationException.ApplicationInstanceErrorType.INVALID_REQUEST);
            }
        } catch (HttpServerErrorException _502) {
            logger.error("Could not create an instance of application " + appId + " - " + instancePattern.getName(), _502);
            throw new ApplicationInstanceCreationException(appId, instancePattern, ApplicationInstanceCreationException.ApplicationInstanceErrorType.TECHNICAL_ERROR);
        }

    }

    @Override
    @Cacheable("services-of-instance")
    public List<CatalogEntry> findServicesOfInstance(String instanceId) {
        logger.debug("Finding services of instance {}", instanceId);

        CatalogEntry[] body = kernel.exchange(appsEndpoint + "/instance/{instance_id}/services", HttpMethod.GET, null, CatalogEntry[].class, user(), instanceId).getBody();
        if (body != null) {
            return Arrays.asList(body);
        } else {
            logger.error("Empty services collection found for instance {}", instanceId);
            return Collections.emptyList();
        }

    }

    @Override
    @Cacheable("instances")
    public ApplicationInstance findApplicationInstance(String instanceId) {
        return kernel.exchange(appsEndpoint + "/instance/{instance_id}", HttpMethod.GET, null, ApplicationInstance.class, user(), instanceId).getBody();
    }


    @Override
    @CachePut(value = "services", key = "#result.id")
    public CatalogEntry fetchAndUpdateService(String serviceId, CatalogEntry service) {

        // we need to be sure to grab everything from the original

        ResponseEntity<KernelService> entity = kernel.exchange(appsEndpoint + "/service/{service_id}", HttpMethod.GET, null, KernelService.class, user(), serviceId);
        String etag = entity.getHeaders().get("ETag").get(0);

        KernelService kernelService = entity.getBody();
        
        // copy / map localized props :
        // TODO Q could hl be also used in GET id ?? then those service props are already localized
        // TODO are find & GET id consistent on this point ??
        kernelService.name = service.getDefaultName();
        kernelService.description = service.getDefaultDescription();
        kernelService.icon = service.getDefaultIcon(); // TODO LATER separate custom'd icon from provider's defaultIcon
        // OR take it from application ?? :
        /*if (service.getDefaultIcon() != null && !service.getDefaultIcon().trim().isEmpty()) {
            kernelService.icon = service.getDefaultIcon();
        } else {
            ApplicationInstance app = cachedService.findApplicationInstance(service.getInstanceId());
            kernelService.icon = app.getIcon();
        }*/

        // kinda hacky, but we'll change that when the UI fully supports l10n
        // TODO can name & description be updated ? then which behaviour outside current locale ???
        for (Locale locale : OasisLocales.values()) {
            kernelService.set("name#" + locale.getLanguage(), service.getDefaultName());
            kernelService.set("description#" + locale.getLanguage(), service.getDefaultDescription());
            kernelService.set("icon#" + locale.getLanguage(), service.getDefaultIcon());
        }

        // copy / map other props :
        // TODO Q could hl be also used in GET id ?? then only if hl was not used
        kernelService.supported_locales = service.getSupportedLocales();
        kernelService.geographical_areas = service.getGeographicalAreas();
        kernelService.restricted_areas = service.getRestrictedAreas();
        kernelService.visible = service.isVisible();

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", etag);

        return kernel.exchange(appsEndpoint + "/service/{service_id}", HttpMethod.PUT, new HttpEntity<>(kernelService, headers), CatalogEntry.class, user(), serviceId).getBody();
    }

    @Override
    public String setInstanceStatus(String instanceId, InstantiationStatus status) {
        logger.warn("Deleting instance {}", instanceId);

        ResponseEntity<ApplicationInstance> instanceEntity = kernel.exchange(appsEndpoint + "/instance/{instance_id}", HttpMethod.GET, null, ApplicationInstance.class, user(), instanceId);
        String eTag = instanceEntity.getHeaders().getETag();

        // clear local mongo cache :
        InstalledStatus installedStatus = installedStatusRepository.findByCatalogEntryTypeAndCatalogEntryIdAndUserId(CatalogEntryType.APPLICATION, instanceEntity.getBody().getApplicationId(), userInfoHelper.currentUser().getUserId());
        if (installedStatus != null) {
            installedStatusRepository.delete(installedStatus);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", eTag); // only if we have the up-to-date version
        
        ApplicationInstance instance = instanceEntity.getBody();
        instance.setStatus(status);
        ResponseEntity<String> resEntity = kernel.exchange(appsEndpoint + "/instance/{instance_id}", HttpMethod.POST,
                new HttpEntity<ApplicationInstance>(instance, headers), String.class, user(), instanceId);
        if (resEntity.getStatusCode().is4xxClientError() || resEntity.getStatusCode().is5xxServerError()) {
            String res = resEntity.getBody();
            if (res != null && !res.trim().isEmpty()) {
                return res; // error message if any
            }
        }
        return null;
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
        @JsonProperty("target_audience")
        List<Audience> audience;
        List<String> category_ids;
        boolean visible;
        String local_id;
        String instance_id;
        String service_uri;
        String notification_uri;
        List<String> redirect_uris;
        List<String> post_logout_redirect_uris;
        List<String> supported_locales;
        Set<String> geographical_areas;
        Set<String> restricted_areas;
        String subscription_id;
        String subscription_secret;
        @JsonProperty("contacts")
        List<String> contacts;
        @JsonProperty("screenshot_uris")
        List<String> screenshotUris;

        @JsonIgnore
        private Map<String, String> otherProperties = new HashMap<>();

        @JsonAnySetter
        public void set(String key, String value) {
            if (!BLACKLISTED.contains(key)) {
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

        public List<Audience> getAudience() {
            return audience;
        }

        public void setAudience(List<Audience> audience) {
            this.audience = audience;
        }

        public List<String> getCategory_ids() {
            return category_ids;
        }

        public void setCategory_ids(List<String> category_ids) {
            this.category_ids = category_ids;
        }
        
        public List<String> getSupported_locales() {
            return supported_locales;
        }

        public void setSupported_locales(List<String> supported_locales) {
            this.supported_locales = supported_locales;
        }

        public Set<String> getGeographical_areas() {
            return geographical_areas;
        }

        public void setGeographical_areas(Set<String> geographical_areas) {
            this.geographical_areas = geographical_areas;
        }

        public Set<String> getRestricted_areas() {
            return restricted_areas;
        }

        public void setRestricted_areas(Set<String> restricted_areas) {
            this.restricted_areas = restricted_areas;
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

        public List<String> getContacts() {
            return contacts;
        }

        public void setContacts(List<String> contacts) {
            this.contacts = contacts;
        }

        public List<String> getScreenshotUris() {
            return screenshotUris;
        }

        public void setScreenshotUris(List<String> screenshotUris) {
            this.screenshotUris = screenshotUris;
        }
    }
}
