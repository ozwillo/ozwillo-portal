package org.oasis_eu.portal.services;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.appstore.*;
import org.oasis_eu.portal.core.model.catalog.Audience;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.core.model.catalog.PaymentOption;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.model.appstore.AcquisitionStatus;
import org.oasis_eu.portal.model.appstore.AppInfo;
import org.oasis_eu.portal.model.appstore.AppstoreHit;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

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
    private MessageSource messageSource;

    public List<AppstoreHit> getAll(List<Audience> targetAudiences) {
        Set<String> subscriptions = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId()).stream().map(Subscription::getServiceId).collect(Collectors.toSet());

        return catalogStore.findAllVisible(targetAudiences).stream()
                .map(c -> new AppstoreHit(RequestContextUtils.getLocale(request), c, organizationStore.find(c.getProviderId()).getName(),
                        subscriptions.contains(c.getId()) ? AcquisitionStatus.INSTALLED : AcquisitionStatus.AVAILABLE))
                .collect(Collectors.toList());
    }

    public AppInfo getInfo(String appId, CatalogEntryType appType) {
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

        return new AppInfo(appId, entry.getName(locale), entry.getDescription(locale), entry.getPaymentOption().equals(PaymentOption.FREE) ? messageSource.getMessage("store.it_is_free", new Object[0], locale) : messageSource.getMessage("store.it_requires_payment", new Object[0], locale), entry.getType());
    }

    public void buy(String appId, CatalogEntryType appType) {

        logger.debug("Buying application {} of type {}", appId, appType);

        if (CatalogEntryType.APPLICATION.equals(appType)) {

            ApplicationInstantiationRequest instanceRequest = new ApplicationInstantiationRequest();

//        instanceRequest.setProviderId(userDirectory.getMemberships(userInfoHelper.currentUser().getUserId()).get(0).getOrganizationId());
            instanceRequest.setProviderId(userInfoHelper.currentUser().getOrganizationId());

            catalogStore.instantiate(appId, instanceRequest);
        } else if (CatalogEntryType.SERVICE.equals(appType)) {

            Subscription subscription = new Subscription();
            subscription.setId(UUID.randomUUID().toString());

            subscription.setSubscriptionType(SubscriptionType.PERSONAL);
            subscription.setUserId(userInfoHelper.currentUser().getUserId());
            subscription.setServiceId(appId);
//            subscription.setCreatorId(userInfoHelper.currentUser().getUserId());

            subscriptionStore.create(userInfoHelper.currentUser().getUserId(), subscription);
        }


    }
}
