package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstantiationRequest;
import org.oasis_eu.portal.core.model.catalog.*;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.core.mongo.dao.store.InstalledStatusRepository;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.mongo.model.store.InstalledStatus;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.model.app.instance.MyAppsInstance;
import org.oasis_eu.portal.model.app.store.AppstoreHit;
import org.oasis_eu.portal.model.app.store.InstallationOption;
import org.oasis_eu.portal.model.user.UserProfile;
import org.oasis_eu.portal.services.dc.geoarea.GeographicalAreaService;
import org.oasis_eu.portal.services.kernel.UserProfileService;
import org.oasis_eu.spring.kernel.model.Organization;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/25/14
 */
@Service
public class AppstoreService {

    private static final Logger logger = LoggerFactory.getLogger(AppstoreService.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CatalogStore catalogStore;

    @Autowired
    private OrganizationStore organizationStore;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private ImageService imageService;

    @Autowired
    GeographicalAreaService geographicalAreaService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private InstalledStatusRepository installedStatusRepository;

    @Value("${application.store.addCurrentToSupportedLocalesIfNone:false}")
    private boolean addCurrentToSupportedLocalesIfNone;


    /**
     * Mirrors Kernel API
     *
     * @param targetAudiences
     * @param paymentOptions
     * @param supportedLocales
     * @param geographicalAreas actually ancestors of the geo area chosen in the autocomplete field
     * @param categoryIds
     * @param q
     * @param from
     * @return
     */
    public List<AppstoreHit> getAll(List<Audience> targetAudiences, List<PaymentOption> paymentOptions,
        List<Locale> supportedLocales, List<String> geographicalAreas,
        List<String> categoryIds, String q, int from) {

        if (addCurrentToSupportedLocalesIfNone) {
            supportedLocales = (supportedLocales == null || supportedLocales.isEmpty()) ?
                Arrays.asList(RequestContextUtils.getLocale(request)) : supportedLocales;
            // TODO or rather use PortalController.currentLanguage() ?? anyway, rather init it on client js side ?!!
        }

        String currentHl = RequestContextUtils.getLocale(request).getLanguage(); // optimization
        List<CatalogEntry> catalogEntryLst = catalogStore.findAllVisible(targetAudiences, paymentOptions, supportedLocales,
            geographicalAreas, categoryIds, q, currentHl, from);

        return catalogEntryLst.stream().filter(catalogEntry -> catalogEntry != null)
            .map(catalogEntry -> new AppstoreHit(RequestContextUtils.getLocale(request), catalogEntry,
                imageService.getImageForURL(catalogEntry.getIcon(RequestContextUtils.getLocale(request)), ImageFormat.PNG_64BY64, false),
                getOrganizationName(catalogEntry), getInstallationOption(catalogEntry)))
            .collect(Collectors.toList());
    }

    private String getOrganizationName(CatalogEntry catalogEntry) {

        String providerId = catalogEntry.getProviderId();
        if (providerId == null) {
            logger.warn("Catalog entry {} - {} has null provider id", catalogEntry.getId(), catalogEntry.getName());
            return "";
        }
        Organization organization = organizationStore.find(providerId);
        if (organization == null) {
            logger.warn("Catalog entry {} - {} has a provider id ({}) that does not correspond to any known organization", catalogEntry.getId(), catalogEntry.getName(), providerId);
            return "";
        }
        return organization.getName();
    }

    public AppstoreHit getInfo(String appId, CatalogEntryType appType) {
        Locale locale = RequestContextUtils.getLocale(request);

        CatalogEntry entry;
        if (CatalogEntryType.APPLICATION.equals(appType)) {
            entry = catalogStore.findApplication(appId);
        } else if (CatalogEntryType.SERVICE.equals(appType)) {
            entry = catalogStore.findService(appId);
        } else {
            logger.warn("Getting information about something that isn't an application or a service: {}", appType);
            throw new IllegalArgumentException("getInfo supports only applications and services");
        }

        String providerName = getOrganizationName(entry);

        return new AppstoreHit(locale, entry, imageService.getImageForURL(entry.getIcon(locale), ImageFormat.PNG_64BY64, false), providerName, getInstallationOption(entry));

    }


    public void buy(String appId, CatalogEntryType appType, String organizationId) {
        logger.debug("Buying application {} of type {}", appId, appType);

        if (CatalogEntryType.APPLICATION.equals(appType)) {
            buyApplication(appId, organizationId);
        } else if (CatalogEntryType.SERVICE.equals(appType)) {
            buyService(appId);
        }
    }

    public MyAppsInstance buyApplication(String appId, String organizationId) {
        CatalogEntry application = catalogStore.findApplication(appId);

        ApplicationInstantiationRequest instanceRequest = new ApplicationInstantiationRequest();
        instanceRequest.setProviderId(organizationId);
        instanceRequest.setName(application.getName(RequestContextUtils.getLocale(request))); // TODO make this user-provided at some stage
        instanceRequest.setDescription(application.getDescription(RequestContextUtils.getLocale(request)));

        ApplicationInstance instance = catalogStore.instantiate(appId, instanceRequest);
        return applicationService.fetchInstance(instance, true);
    }

    public Subscription buyService(String appId) {
        Subscription subscription = new Subscription();
        subscription.setId(UUID.randomUUID().toString());

        subscription.setSubscriptionType(SubscriptionType.PERSONAL);
        subscription.setUserId(userInfoService.currentUser().getUserId());
        subscription.setServiceId(appId);

        return subscriptionStore.create(userInfoService.currentUser().getUserId(), subscription);
    }

    public void updateUserInfo(String name, String lastName, String email, String street, String zip, String city, String country) {
        boolean proceedUpdate = false;
        UserProfile userProfile = userProfileService.findUserProfile(userInfoService.currentUser().getUserId());
        if ((name != null || lastName != null || email != null)
            && (!name.isEmpty() || !lastName.isEmpty() || !email.isEmpty())
            && (!userProfile.getGivenName().equals(name) || !userProfile.getFamilyName().equals(lastName))) {
            userProfile.setGivenName(name);
            userProfile.setFamilyName(lastName);
            proceedUpdate = true;
        }

        if ((zip != null && city != null && country != null)
            && (!zip.isEmpty() && !city.isEmpty() && !country.isEmpty())) { // not always as a required value
            org.oasis_eu.spring.kernel.model.Address address = userProfile.getAddress();
            address.setStreetAddress(street);
            address.setPostalCode(zip);
            address.setLocality(city);
            address.setCountry(country);
            userProfile.setAddress(address);
            proceedUpdate = true;
        }

        if (proceedUpdate && userInfoService.isAuthenticated()) {
            userProfileService.saveUserProfile(userProfile);
        }
    }

    private InstallationOption getInstallationOption(CatalogEntry entry) {
        InstallationOption paymentOption = InstallationOption.valueOf(entry.getPaymentOption().toString());
        if (!userInfoService.isAuthenticated()) {
            return paymentOption; // urgh. clean this up sometime!
        }

        InstalledStatus status = installedStatusRepository.findByCatalogEntryTypeAndCatalogEntryIdAndUserId(entry.getType(), entry.getId(), userInfoService.currentUser().getUserId());
        if (status != null) {
            return status.isInstalled() ? InstallationOption.INSTALLED : paymentOption;
        }

        InstallationOption option = computeInstallationOption(entry);

        status = new InstalledStatus();
        status.setCatalogEntryType(entry.getType());
        status.setCatalogEntryId(entry.getId());
        status.setUserId(userInfoService.currentUser().getUserId());
        status.setInstalled(option.equals(InstallationOption.INSTALLED));
        installedStatusRepository.save(status);

        return option;
    }

    private InstallationOption computeInstallationOption(CatalogEntry entry) {
        if (CatalogEntryType.SERVICE.equals(entry.getType())) {
            Set<String> subscriptions = subscriptionStore.findByUserId(userInfoService.currentUser().getUserId()).stream().map(Subscription::getServiceId).collect(Collectors.toSet());
            return subscriptions.contains(entry.getId()) ? InstallationOption.INSTALLED :
                PaymentOption.FREE.equals(entry.getPaymentOption()) ? InstallationOption.FREE : InstallationOption.PAID;
        } else {
            return networkService.getMyAuthorities(true).stream()
                .flatMap(authority -> applicationService.getMyInstances(authority, false).stream())
                .anyMatch(instance -> instance.getApplicationInstance().getApplicationId().equals(entry.getId()))
                ? InstallationOption.INSTALLED :
                PaymentOption.FREE.equals(entry.getPaymentOption()) ? InstallationOption.FREE : InstallationOption.PAID;
        }
    }
}
