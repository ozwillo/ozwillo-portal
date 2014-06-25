package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.OrganizationStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.appstore.Audience;
import org.oasis_eu.portal.core.model.appstore.CatalogEntry;
import org.oasis_eu.portal.core.model.appstore.PaymentOption;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.model.AcquisitionStatus;
import org.oasis_eu.portal.model.AppInfo;
import org.oasis_eu.portal.model.AppstoreHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/25/14
 */
@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PortalAppstoreService {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CatalogStore catalogStore;

    @Autowired
    private OrganizationStore organizationStore;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Autowired
    private UserInfoHelper userInfoHelper;

    @Autowired
    private MessageSource messageSource;

    public List<AppstoreHit> getAll(List<Audience> targetAudiences) {
        Map<String, Subscription> subscriptions = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId()).stream()
                .collect(Collectors.toMap(Subscription::getId, s -> s));

        return catalogStore.findAllVisible(targetAudiences).stream()
                .map(c -> new AppstoreHit(RequestContextUtils.getLocale(request), c, organizationStore.find(c.getProviderId()).getName(),
                        subscriptions.containsKey(c.getId()) || subscriptions.containsKey(c.getParentId()) ? AcquisitionStatus.INSTALLED : AcquisitionStatus.AVAILABLE))
                .collect(Collectors.toList());
    }

    public AppInfo getInfo(String appId) {
        Locale locale = RequestContextUtils.getLocale(request);

        CatalogEntry entry = catalogStore.find(appId);

        return new AppInfo(appId, entry.getName(locale), entry.getDescription(locale), entry.getPaymentOption().equals(PaymentOption.FREE) ? messageSource.getMessage("store.it_is_free", new Object[0], locale) : messageSource.getMessage("store.it_requires_payment", new Object[0], locale));
    }

    public void buy(String appId) {
        catalogStore.subscribe(appId, userInfoHelper.currentUser().getUserId());
    }
}
