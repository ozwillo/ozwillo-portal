package org.oasis_eu.portal.front.store;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.markdown4j.Markdown4jProcessor;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstanceCreationException;
import org.oasis_eu.portal.core.model.catalog.Audience;
import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.core.model.catalog.PaymentOption;
import org.oasis_eu.portal.model.appstore.AppstoreHit;
import org.oasis_eu.portal.services.PortalAppstoreService;
import org.oasis_eu.spring.kernel.model.Organization;
import org.oasis_eu.spring.kernel.model.OrganizationType;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 10/29/14
 */
@RestController
@RequestMapping("/api/store")
public class StoreAJAXServices {

    private static final Logger logger = LoggerFactory.getLogger(StoreAJAXServices.class);


    @Autowired
    private PortalAppstoreService appstoreService;

    @Autowired
    private OrganizationStore organizationStore;

    @RequestMapping(value = "/applications", method = RequestMethod.GET)
    public List<StoreApplication> applications(@RequestParam boolean target_citizens,
                                               @RequestParam boolean target_publicbodies,
                                               @RequestParam boolean target_companies,
                                               @RequestParam boolean free,
                                               @RequestParam boolean paid) {

        logger.debug("Loading applications...");

        List<Audience> audiences = new ArrayList<Audience>();
        if (target_citizens) audiences.add(Audience.CITIZENS);
        if (target_publicbodies) audiences.add(Audience.PUBLIC_BODIES);
        if (target_companies) audiences.add(Audience.COMPANIES);

        List<PaymentOption> paymentOptions = new ArrayList<>();
        if (free) paymentOptions.add(PaymentOption.FREE);
        if (paid) paymentOptions.add(PaymentOption.PAID);

        return appstoreService.getAll(audiences, paymentOptions).stream()
                .map(this::toStoreApplication)
                .collect(Collectors.toList());
    }

    @RequestMapping("/details/{type}/{id}")
    public ApplicationDetails applicationDetails(@PathVariable String type, @PathVariable String id) {

        AppstoreHit hit = appstoreService.getInfo(id, CatalogEntryType.valueOf(type.toUpperCase()));

        return toApplicationDetails(hit);
    }

    @RequestMapping(value = "/buy", method = RequestMethod.POST)
    public StoreBuyStatus buy(@RequestBody StoreBuyRequest request) {
        StoreBuyStatus response = new StoreBuyStatus();
        try {
            appstoreService.buy(request.appId, CatalogEntryType.valueOf(request.appType.toUpperCase()), request.organizationId);
            response.success = true;
        } catch (ApplicationInstanceCreationException e) {
            response.success = false;
        }
        return response;
    }

    private StoreApplication toStoreApplication(AppstoreHit hit) {
        StoreApplication application = new StoreApplication();
        application.type = StoreApplication.Type.valueOf(hit.getType().toLowerCase());
        application.id = hit.getId();
        application.audienceCitizens = hit.getCatalogEntry().getTargetAudience().stream().anyMatch(audience -> audience.equals(Audience.CITIZENS));
        application.audiencePublicBodies = hit.getCatalogEntry().getTargetAudience().stream().anyMatch(audience -> audience.equals(Audience.PUBLIC_BODIES));
        application.audienceCompanies = hit.getCatalogEntry().getTargetAudience().stream().anyMatch(audience -> audience.equals(Audience.COMPANIES));
        application.description = hit.getDescription();
        application.icon = hit.getIconUrl();
        application.name = hit.getName();
        application.paid = hit.getCatalogEntry().getPaymentOption().equals(PaymentOption.PAID);
        application.providerName = hit.getProviderName();
        String providerId = hit.getCatalogEntry().getProviderId();

        // let's be paranoid about nulls here
        if (providerId != null) {
            Organization organization = organizationStore.find(providerId);
            if (organization != null) {
                OrganizationType type = organization.getType();
                if (type != null) {
                    application.publicService = type.equals(OrganizationType.PUBLIC_BODY);
                } else {
                    logger.warn("{} is a provider without a type", organization.getName());
                }
            } else {
                logger.warn("{} is not a known provider", providerId);
            }
        } else {
            logger.warn("{} has a null provider", hit.getName());
        }

        return application;
    }

    private ApplicationDetails toApplicationDetails(AppstoreHit hit) {
        ApplicationDetails applicationDetails = new ApplicationDetails();

        try {
            applicationDetails.longdescription = new Markdown4jProcessor().process(hit.getLongDescription().replaceAll("[<>]", ""));
        } catch (IOException e) {
            logger.error("Cannot perform Markdown conversion", e);
            applicationDetails.longdescription = hit.getLongDescription();
        }

        applicationDetails.policy = hit.getCatalogEntry().getPolicyUri();
        applicationDetails.tos = hit.getCatalogEntry().getTosUri();
        applicationDetails.rating = 0;
        applicationDetails.screenshots = hit.getCatalogEntry().getScreenshotUris();

        return applicationDetails;
    }

    private static class StoreBuyRequest {
        @JsonProperty
        String appId;
        @JsonProperty
        String appType;
        @JsonProperty
        String organizationId;
    }

    private static class StoreBuyStatus {
        @JsonProperty
        boolean success;
    }

}
