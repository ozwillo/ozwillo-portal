package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstantiationRequest;
import org.oasis_eu.portal.core.model.catalog.Audience;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.core.model.catalog.PaymentOption;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.core.services.icons.IconService;
import org.oasis_eu.portal.model.appstore.AppstoreHit;
import org.oasis_eu.portal.model.appstore.InstallationOption;
import org.oasis_eu.spring.kernel.model.Organization;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/25/14
 */
@Service
public class PortalAppstoreService {

    private static final Logger logger = LoggerFactory.getLogger(PortalAppstoreService.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CatalogStore catalogStore;

    @Autowired
    private OrganizationStore organizationStore;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Autowired
    private UserInfoService userInfoHelper;

    @Autowired
    private IconService iconService;

    @Autowired
    private UserDirectory userDirectory;

    @Autowired
    private PortalAppManagementService appManagementService;

    public List<AppstoreHit> getAll(List<Audience> targetAudiences) {

        return catalogStore.findAllVisible(targetAudiences).stream()
                .map(catalogEntry -> new AppstoreHit(RequestContextUtils.getLocale(request), catalogEntry, iconService.getIconForURL(catalogEntry.getIcon(RequestContextUtils.getLocale(request))), getOrganizationName(catalogEntry),
                        getInstallationOption(catalogEntry)))
                .collect(Collectors.toList());
    }

    private String getOrganizationName(CatalogEntry catalogEntry) {
        String providerId = catalogEntry.getProviderId();
        if (providerId == null) {
            logger.warn("Catalog entry {} - {} has null provider id", catalogEntry.getId(), catalogEntry.getDefaultName());
            return "";
        }
        Organization organization = organizationStore.find(providerId);
        if (organization == null) {
            logger.warn("Catalog entry {} - {} has a provider id ({}) that does not correspond to any known organization", catalogEntry.getId(), catalogEntry.getDefaultName(), providerId);
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

        Organization organization = organizationStore.find(entry.getProviderId());
        String providerName = organization != null ? organization.getName() : "-";

        return new AppstoreHit(locale, entry, iconService.getIconForURL(entry.getIcon(locale)), providerName, getInstallationOption(entry));

    }


    public void buy(String appId, CatalogEntryType appType, String organizationId) {

        logger.debug("Buying application {} of type {}", appId, appType);

        if (CatalogEntryType.APPLICATION.equals(appType)) {
            CatalogEntry application = catalogStore.findApplication(appId);

            ApplicationInstantiationRequest instanceRequest = new ApplicationInstantiationRequest();

            instanceRequest.setProviderId(organizationId);
            instanceRequest.setName(application.getName(RequestContextUtils.getLocale(request))); // TODO make this user-provided at some stage
            instanceRequest.setDescription(application.getDescription(RequestContextUtils.getLocale(request)));

            catalogStore.instantiate(appId, instanceRequest);
        } else if (CatalogEntryType.SERVICE.equals(appType)) {

            Subscription subscription = new Subscription();
            subscription.setId(UUID.randomUUID().toString());

            subscription.setSubscriptionType(SubscriptionType.PERSONAL);
            subscription.setUserId(userInfoHelper.currentUser().getUserId());
            subscription.setServiceId(appId);

            subscriptionStore.create(userInfoHelper.currentUser().getUserId(), subscription);
        }


    }

    private InstallationOption getInstallationOption(CatalogEntry entry) {
        if (CatalogEntryType.SERVICE.equals(entry.getType())) {
            Set<String> subscriptions = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId()).stream().map(Subscription::getServiceId).collect(Collectors.toSet());
            return subscriptions.contains(entry.getId()) ? InstallationOption.INSTALLED :
                    PaymentOption.FREE.equals(entry.getPaymentOption()) ? InstallationOption.FREE : InstallationOption.PAYING;
        } else {
            return appManagementService.getMyAuthorities(true).stream()
                    .flatMap(authority -> appManagementService.getMyInstances(authority).stream())
                    .anyMatch(instance -> instance.getApplication().getId().equals(entry.getId()))
                    ? InstallationOption.INSTALLED :
                        PaymentOption.FREE.equals(entry.getPaymentOption()) ? InstallationOption.FREE : InstallationOption.PAYING;

        }
    }
}
