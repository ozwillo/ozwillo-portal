package org.oasis_eu.portal.front.store;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstanceCreationException;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.Audience;
import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.core.model.catalog.PaymentOption;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalArea;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.front.generic.BaseController;
import org.oasis_eu.portal.model.app.instance.MyAppsInstance;
import org.oasis_eu.portal.model.app.service.InstanceService;
import org.oasis_eu.portal.model.app.store.AppstoreHit;
import org.oasis_eu.portal.model.app.store.InstallationOption;
import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.portal.services.ApplicationService;
import org.oasis_eu.portal.ui.UIOrganization;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.portal.services.AppstoreService;
import org.oasis_eu.portal.services.RatingService;
import org.oasis_eu.portal.services.dc.geoarea.GeographicalAreaService;
import org.oasis_eu.portal.services.dc.organization.DCOrganizationService;
import org.oasis_eu.portal.services.dc.organization.DCRegActivity;
import org.oasis_eu.portal.services.dc.organization.DCRegActivityResponse;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.oasis_eu.spring.kernel.model.Organization;
import org.oasis_eu.spring.kernel.model.OrganizationStatus;
import org.oasis_eu.spring.kernel.model.OrganizationType;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * User: schambon
 * Date: 10/29/14
 */
@RestController
@RequestMapping("/api/store")
public class StoreController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);


    @Autowired
    private AppstoreService appstoreService;

    @Autowired
    private OrganizationStore organizationStore;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private GeographicalAreaService geographicalAreaService;

    @Autowired
    private DCOrganizationService organizationService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CatalogStore catalogStore;

    @Value("${application.store.load_size:20}")
    private int loadSize;


    @RequestMapping(value = "/geographicalAreas", method = RequestMethod.GET)
    public GeographicalAreaResponse geographicalAreas(@RequestParam String country_uri, @RequestParam String q) {
        int areaLoadSize = 10;
        int areaDcLoadSize = areaLoadSize + 1;
        List<GeographicalArea> areas = geographicalAreaService.find(country_uri, null, q, 0, areaDcLoadSize);

        return new GeographicalAreaResponse(areas.stream()
            .limit(areaLoadSize).collect(Collectors.toList()), areas.size() == areaDcLoadSize);
    }

    @RequestMapping(value = "/dc-taxRegActivity", method = GET)
    public DCRegActivityResponse searchTaxRegActivity(@RequestParam String country_uri, @RequestParam String q) {
        logger.debug("Searching for RegActivity {} from {} ", q, country_uri);
        List<DCRegActivity> TaxRegActivityLst = organizationService.searchTaxRegActivity(country_uri, q, 0, 10);
        return new DCRegActivityResponse(TaxRegActivityLst);
    }

    @RequestMapping(value = "/dc-cities", method = RequestMethod.GET)
    public GeographicalAreaResponse dcCities(@RequestParam String country_uri, @RequestParam String q) {
        int loadSize = 10;
        List<GeographicalArea> cities = geographicalAreaService.findCities(q, country_uri, 0, loadSize + 1);

        return new GeographicalAreaResponse(
            cities.stream().limit(loadSize).collect(Collectors.toList()), (cities.size() == loadSize + 1));
    }

    @RequestMapping(value = "/dc-countries", method = RequestMethod.GET)
    public GeographicalAreaResponse dcCountries(@RequestParam String q) {
        int loadSize = 10;
        List<GeographicalArea> countries = geographicalAreaService.findCountries(q);

        return new GeographicalAreaResponse(
            countries.stream().limit(loadSize).collect(Collectors.toList()), (countries.size() == loadSize + 1));
    }


    @RequestMapping(value = "/applications", method = RequestMethod.GET)
    public StoreAppResponse applications(
        @RequestParam boolean target_citizens,
        @RequestParam boolean target_publicbodies,
        @RequestParam boolean target_companies,
        @RequestParam boolean free,
        @RequestParam boolean paid,
        @RequestParam(required = false) List<String> supported_locales,
        @RequestParam(required = false) List<String> geoArea_AncestorsUris,
        @RequestParam(required = false) List<String> category_ids,
        @RequestParam(required = false) String q,
        @RequestParam(required = false, defaultValue = "0") int last) {

        logger.debug("Loading applications...");
        logger.debug("last = {}", last);

        List<Audience> audiences = new ArrayList<>();
        if (target_citizens) audiences.add(Audience.CITIZENS);
        if (target_publicbodies) audiences.add(Audience.PUBLIC_BODIES);
        if (target_companies) audiences.add(Audience.COMPANIES);

        List<PaymentOption> paymentOptions = new ArrayList<>();
        if (free) paymentOptions.add(PaymentOption.FREE);
        if (paid) paymentOptions.add(PaymentOption.PAID);

        List<Locale> supportedLocales = supported_locales == null ? null : supported_locales.stream()
            .map(localeString -> Locale.forLanguageTag(localeString)).collect(Collectors.toList());
        List<StoreApplication> apps = appstoreService.getAll(audiences, paymentOptions,
            supportedLocales, geoArea_AncestorsUris, category_ids, q, last).stream()
            .map(this::toStoreApplication)
            .collect(Collectors.toList());
        //apps = new ArrayList<StoreApplication>(); // for easy testing

        return new StoreAppResponse(apps, apps.size() == loadSize); // if we got exactly as many as we'd have liked, there are likely more
    }

    /**
     * for loading default_app, else when direct link is not displayed if not in first page see #152
     */
    @RequestMapping("/application/{type}/{id}")
    public StoreApplication application(@PathVariable String type, @PathVariable String id) {
        AppstoreHit hit = appstoreService.getInfo(id, CatalogEntryType.valueOf(type.toUpperCase()));
        return toStoreApplication(hit);
    }

    @RequestMapping("/details/{type}/{id}")
    public ApplicationDetails applicationDetails(@PathVariable String type, @PathVariable String id) {
        AppstoreHit hit = appstoreService.getInfo(id, CatalogEntryType.valueOf(type.toUpperCase()));
        return toApplicationDetails(hit);
    }

    @RequestMapping(value = "/buy", method = RequestMethod.POST)
    public void buy(@RequestBody StoreBuyRequest request) {
        appstoreService.buy(request.appId, CatalogEntryType.valueOf(request.appType.toUpperCase()), request.organizationId);

        //Update user details contained in StoreBuyRequest, usually contact & address (address if personal service install)
        appstoreService.updateUserInfo(request.contact_name, request.contact_lastname, request.contact_email,
            request.street_and_number, request.zip, request.city, request.country);

        //Create ACLs
        //TODO may be for next feature: Add an instance to an organization and create in same time ACLs for users
        //
        /*if (request.members != null) {
           request.members.forEach( user -> applicationService.createAcl(request.appId, user));
        }*/
    }

    @PostMapping("/buy/application")
    public MyAppsInstance buyApplication(@RequestBody StoreBuyRequest request) {
        return appstoreService.buyApplication(request.appId, request.organizationId);
    }

    @PostMapping("/buy/service/{serviceId}")
    public Subscription buyApplication(@PathVariable String serviceId) {
        return appstoreService.buyService(serviceId);
    }

    @RequestMapping(value = "/rate/{appType}/{appId}", method = RequestMethod.POST)
    public void rate(@PathVariable String appType, @PathVariable String appId, @RequestBody RateRequest rateRequest) {
        ratingService.rate(appType, appId, rateRequest.rate);
    }

    /**
     * called by store to get organizations in which an app or service may be installed (org dropdown in the app modal)
     */
    @RequestMapping(value = "/organizations/{appType}/{appId}", method = GET)
    public List<UIOrganization> organizations(@PathVariable String appType, @PathVariable String appId) {

        AppstoreHit info = appstoreService.getInfo(appId, CatalogEntryType.valueOf(appType.toUpperCase())); // #152 services can also be installed
        List<UIOrganization> organizations = networkService.getMyOrganizations();

        return organizations.stream()
            .filter(o -> o.isAdmin())
            .filter(o -> OrganizationStatus.AVAILABLE.equals(o.getStatus())) // not deleted ones
            .filter(o -> info.getCatalogEntry().getTargetAudience().stream().anyMatch(audience -> audience.isCompatibleWith(o.getType())))
            .collect(Collectors.toList());
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
        application.installed = hit.getInstallationOption().equals(InstallationOption.INSTALLED);

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

//		try {
//			applicationDetails.longdescription = new Markdown4jProcessor().process(hit.getLongDescription().replaceAll("[<>]", ""));
//		} catch (IOException e) {
//			logger.error("Cannot perform Markdown conversion", e);
        applicationDetails.longdescription = hit.getLongDescription();
//		}

        applicationDetails.policy = hit.getCatalogEntry().getPolicyUri();
        applicationDetails.tos = hit.getCatalogEntry().getTosUri();
        applicationDetails.rating = ratingService.getRating(hit.getType(), hit.getId());
        applicationDetails.rateable = ratingService.isRateable(hit.getCatalogEntry().getType(), hit.getId());
        List<String> screenshotUris = hit.getCatalogEntry().getScreenshotUris();
        if (screenshotUris != null) {
            applicationDetails.screenshots = screenshotUris.stream()
                .map(uri -> imageService.getImageForURL(uri, ImageFormat.PNG_800BY450, false))
                .filter(uri -> !imageService.isDefaultIcon(uri)).collect(Collectors.toList());
        }

        return applicationDetails;
    }

    private static class StoreBuyRequest {
        @JsonProperty
        String appId;
        @JsonProperty
        String appType;
        @JsonProperty
        String organizationId;
        //to update user details
        @JsonProperty
        String contact_name;
        @JsonProperty
        String contact_lastname;
        @JsonProperty
        String contact_email;
        @JsonProperty
        String additional_address_field;
        @JsonProperty
        String street_and_number;
        @JsonProperty
        String city_uri;
        @JsonProperty
        String city;
        @JsonProperty
        String zip;
        @JsonProperty
        String country_uri;
        @JsonProperty
        String country;
        @JsonProperty
        List<User> members;
    }


    private static class RateRequest {
        @JsonProperty
        double rate;
    }
}
