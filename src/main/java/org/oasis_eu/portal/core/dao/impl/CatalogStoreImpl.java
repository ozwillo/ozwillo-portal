package org.oasis_eu.portal.core.dao.impl;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstanceCreationException;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstantiationRequest;
import org.oasis_eu.portal.core.model.catalog.*;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance.InstantiationStatus;
import org.oasis_eu.portal.core.mongo.dao.store.InstalledStatusRepository;
import org.oasis_eu.portal.core.mongo.model.store.InstalledStatus;
import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.service.Kernel;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import static org.oasis_eu.spring.kernel.model.AuthenticationBuilder.*;

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

    @Value("${application.store.load_size:24}")
    private int loadSize;

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
        if (supportedLocales != null)
            supportedLocales.stream().forEach(supportedLocale -> uriBuilder.queryParam("supported_locale", supportedLocale.getLanguage()));
        if (geographicalAreas != null)
            geographicalAreas.stream().forEach(geographicalArea -> uriBuilder.queryParam("geographical_areas", geographicalArea));
        //if (restrictedAreas != null) restrictedAreas.stream().forEach(restrictedArea -> uriBuilder.queryParam("restricted_areas", restrictedArea)); // TODO from user locale !?
        if (categoryIds != null)
            categoryIds.stream().forEach(categoryId -> uriBuilder.queryParam("category_id", categoryId));
        if (q != null && !q.isEmpty()) {
            uriBuilder.queryParam("q", q);
        }

        String uri = uriBuilder.build().toUriString();

        List<CatalogEntry> catalogEntries = Arrays.asList(kernel.getEntityOrException(uri, CatalogEntry[].class, none()));
        if (logger.isDebugEnabled()) {
            logger.debug("Found catalog entries:");
            catalogEntries.forEach(e -> logger.debug(e.toString()));
        }

        return new ArrayList<>(catalogEntries);
    }

    @Override
    @Cacheable("applications")
    public CatalogEntry findApplication(String id) {
        return kernel.getEntityOrException(appsEndpoint + "/app/{id}", CatalogEntry.class, userIfExists(), id);
    }


    @Override
    @Cacheable("services")
    public ServiceEntry findService(String id) {
        ServiceEntry serviceEntry;

        try {
            serviceEntry = kernel.getEntityOrException(appsEndpoint + "/service/{id}",
                    ServiceEntry.class, userIfExists(), id);
        } catch(HttpClientErrorException e) {
            if (HttpStatus.FORBIDDEN.equals(e.getStatusCode())) {
                throw new WrongQueryException(e.getResponseBodyAsString(), e.getStatusCode().value());
            }

            throw e;
        }

        return serviceEntry;
    }

    @Override
    public ApplicationInstance instantiate(String appId, ApplicationInstantiationRequest instancePattern) throws ApplicationInstanceCreationException {
        logger.info("Application instantiation request: {}", instancePattern);

        InstalledStatus status = installedStatusRepository.findByCatalogEntryTypeAndCatalogEntryIdAndUserId(CatalogEntryType.APPLICATION, appId, userInfoHelper.currentUser().getUserId());
        if (status != null) {
            installedStatusRepository.delete(status);
        }

        ApplicationInstance newInstance;
        try {
            ResponseEntity<ApplicationInstance> responseEntity = kernel.exchange(endpoint + "/instantiate/{appId}", HttpMethod.POST,
                new HttpEntity<>(instancePattern), ApplicationInstance.class, user(), appId);

            newInstance = responseEntity.getBody();

            // specific error handling, TODO LATER make it more consistent with generic error handling
            if (responseEntity.getStatusCode().is4xxClientError()) {
                logger.error("Got a client error when creating an instance of application {} ({}): {}", appId, instancePattern.getName(), responseEntity.getStatusCode().getReasonPhrase());
                throw new ApplicationInstanceCreationException(appId, instancePattern, ApplicationInstanceCreationException.ApplicationInstanceErrorType.INVALID_REQUEST);
            }
        } catch (TechnicalErrorException _502) { // as thrown by the kernel when a HttpServerErrorException 502 occurs
            //} catch (HttpServerErrorException _502) {
            logger.error("Could not create an instance of application " + appId + " - " + instancePattern.getName(), _502);
            throw new ApplicationInstanceCreationException(appId, instancePattern, ApplicationInstanceCreationException.ApplicationInstanceErrorType.TECHNICAL_ERROR);
        }

        return newInstance;

    }

    @Override
    @Cacheable("services-of-instance")
    public List<ServiceEntry> findServicesOfInstance(String instanceId) {
        logger.debug("Finding services of instance {}", instanceId);

        ServiceEntry[] serviceEntries = kernel.getEntityOrException(appsEndpoint + "/instance/{instance_id}/services",
            ServiceEntry[].class, user(), instanceId);
        return Arrays.asList(serviceEntries);
    }

    @Override
    @Cacheable("instances")
    public ApplicationInstance findApplicationInstance(String instanceId) {
        return kernel.getEntityOrException(appsEndpoint + "/instance/{instance_id}", ApplicationInstance.class, user(), instanceId);
    }

    @Override
    @Cacheable("instances")
    public ApplicationInstance findApplicationInstanceOrNull(String instanceId) {
        return kernel.getEntityOrNull(appsEndpoint + "/instance/{instance_id}", ApplicationInstance.class, user(), instanceId);
    }

    @Override
    @CachePut(value = "services", key = "#result.id")
    public ServiceEntry updateService(String serviceId, ServiceEntry service) {
        String uriString = appsEndpoint + "/service/{service_id}";

        // we need to be sure to grab everything from the original
        ResponseEntity<ServiceEntry> entity = kernel.exchange(uriString, HttpMethod.GET, null, ServiceEntry.class, user(), serviceId);
        String etag = entity.getHeaders().get("ETag").get(0);

        ServiceEntry serviceFromKernel = kernel.getBodyUnlessClientError(entity, ServiceEntry.class, uriString, serviceId);

        serviceFromKernel.setName(service.getName());
        serviceFromKernel.setDescription(service.getDescription());
        serviceFromKernel.setIcon(service.getIcon());
        serviceFromKernel.setGeographicalAreas(service.getGeographicalAreas());
        serviceFromKernel.setVisibility(service.getVisibility());
        serviceFromKernel.setAccessControl(service.getAccessControl());

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", etag);

        ResponseEntity<ServiceEntry> kernelResp;
        try {
             kernelResp = kernel.exchange(uriString, HttpMethod.PUT, new HttpEntity<>(serviceFromKernel, headers),
                            ServiceEntry.class, user(), serviceId);
        } catch(RestClientException e) {
            throw new WrongQueryException(e.getMessage(), HttpStatus.BAD_REQUEST.value());
        }


        return kernel.getBodyUnlessClientError(kernelResp, ServiceEntry.class, uriString);
    }

    @Override
    public ApplicationInstance setInstanceStatus(String instanceId, InstantiationStatus status) {
        logger.warn("Deleting instance {}", instanceId);

        ResponseEntity<ApplicationInstance> respAppInstance = kernel.exchange(appsEndpoint + "/instance/{instance_id}",
            HttpMethod.GET, null, ApplicationInstance.class, user(), instanceId);
        String eTag = respAppInstance.getHeaders().getETag();

        // clear local mongo cache :
        InstalledStatus installedStatus = installedStatusRepository.findByCatalogEntryTypeAndCatalogEntryIdAndUserId(CatalogEntryType.APPLICATION,
            respAppInstance.getBody().getApplicationId(), userInfoHelper.currentUser().getUserId());
        if (installedStatus != null) {
            installedStatusRepository.delete(installedStatus);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("If-Match", eTag); // only if we have the up-to-date version

        ApplicationInstance instance = kernel.getBodyUnlessClientError(respAppInstance, ApplicationInstance.class,
            appsEndpoint + "/instance/{instance_id}", instanceId);

        instance.setStatus(status);
        ResponseEntity<ApplicationInstance> resEntity = kernel.exchange(appsEndpoint + "/instance/{instance_id}", HttpMethod.POST,
            new HttpEntity<>(instance, headers), ApplicationInstance.class, user(), instanceId);

        return resEntity.getBody();
    }
}
