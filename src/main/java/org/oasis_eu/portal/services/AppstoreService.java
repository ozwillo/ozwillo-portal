package org.oasis_eu.portal.services;

import org.oasis_eu.portal.model.images.ImageFormat;
import org.oasis_eu.portal.model.instance.MyAppsInstance;
import org.oasis_eu.portal.model.kernel.ApplicationInstantiationRequest;
import org.oasis_eu.portal.model.kernel.instance.ApplicationInstance;
import org.oasis_eu.portal.model.kernel.instance.Subscription;
import org.oasis_eu.portal.model.kernel.instance.SubscriptionType;
import org.oasis_eu.portal.model.kernel.store.Audience;
import org.oasis_eu.portal.model.kernel.store.CatalogEntry;
import org.oasis_eu.portal.model.kernel.store.CatalogEntryType;
import org.oasis_eu.portal.model.kernel.store.PaymentOption;
import org.oasis_eu.portal.model.organization.UIOrganization;
import org.oasis_eu.portal.model.store.AppstoreHit;
import org.oasis_eu.portal.model.store.InstallationOption;
import org.oasis_eu.portal.services.dc.GeographicalAreaService;
import org.oasis_eu.portal.services.kernel.CatalogStoreImpl;
import org.oasis_eu.portal.services.kernel.SubscriptionStoreImpl;
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
    private CatalogStoreImpl catalogStore;

    @Autowired
    private OrganizationStore organizationStore;

    @Autowired
    private SubscriptionStoreImpl subscriptionStore;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private ImageService imageService;

    @Autowired
    GeographicalAreaService geographicalAreaService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OrganizationService organizationService;

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
                                    List<Locale> supportedLocales, String organizationId, String installed_status, List<String> geographicalAreas,
                                    List<String> categoryIds, String q, int from) {

        if (addCurrentToSupportedLocalesIfNone) {
            supportedLocales = (supportedLocales == null || supportedLocales.isEmpty()) ?
                    List.of(RequestContextUtils.getLocale(request)) : supportedLocales;
            // TODO or rather use PortalController.currentLanguage() ?? anyway, rather init it on client js side ?!!
        }

        String currentHl = RequestContextUtils.getLocale(request).getLanguage(); // optimization
        List<CatalogEntry> catalogEntryLst = catalogStore.findAllVisible(targetAudiences, paymentOptions, supportedLocales,
                geographicalAreas, categoryIds, q, currentHl, from);

        //if an organization is selected, display the specifics app with the install option
        if(organizationId != null && !organizationId.equals("")) {
            UIOrganization organization = organizationService.getOrganizationFromKernel(organizationId);

            catalogEntryLst = catalogEntryLst.stream()
                    .filter(app -> app.getTargetAudience().stream().anyMatch(audience -> audience.isCompatibleWith(organization.getType())))
                    .collect(Collectors.toList());

            //return all the applications available for a specific organization with the install option
            return catalogEntryLst.stream()
                    .filter(Objects::nonNull)
                    .map(catalogEntry -> new AppstoreHit(RequestContextUtils.getLocale(request), catalogEntry,
                            imageService.getImageForURL(catalogEntry.getIcon(RequestContextUtils.getLocale(request)), ImageFormat.PNG_64BY64, false),
                            getOrganizationName(catalogEntry), getApplicationInstallationOptionFromOrganization(organization, catalogEntry)))
                    .filter(appstoreHit -> installed_status.equals("") || appstoreHit.getInstallationOption().toString().equals(installed_status.toUpperCase()))
                    .collect(Collectors.toList());
        }

        return catalogEntryLst.stream().filter(Objects::nonNull)
            .map(catalogEntry -> new AppstoreHit(RequestContextUtils.getLocale(request), catalogEntry,
                imageService.getImageForURL(catalogEntry.getIcon(RequestContextUtils.getLocale(request)), ImageFormat.PNG_64BY64, false),
                getOrganizationName(catalogEntry)))
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

        return new AppstoreHit(locale, entry, imageService.getImageForURL(entry.getIcon(locale), ImageFormat.PNG_64BY64, false), providerName);

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

    private InstallationOption getApplicationInstallationOptionFromOrganization(UIOrganization organization, CatalogEntry entry){
        boolean installed = organization.getInstances().stream()
                .map(MyAppsInstance::getApplicationInstance)
                .anyMatch(instance ->
                   entry.getId() != null && entry.getType().equals(CatalogEntryType.APPLICATION) && entry.getId().equals(instance.getApplicationId())
                           || entry.getProviderId() != null && entry.getType().equals(CatalogEntryType.SERVICE) && entry.getProviderId().equals(instance.getProviderId())
                );
        if(installed){
            return InstallationOption.INSTALLED;
        }else{
            return InstallationOption.NOT_INSTALLED;
        }
    }
}
