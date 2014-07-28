package org.oasis_eu.portal.services;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.appstore.AppInstance;
import org.oasis_eu.portal.core.model.appstore.Audience;
import org.oasis_eu.portal.core.model.appstore.CatalogEntry;
import org.oasis_eu.portal.core.model.appstore.PaymentOption;
import org.oasis_eu.portal.model.AcquisitionStatus;
import org.oasis_eu.portal.model.AppInfo;
import org.oasis_eu.portal.model.AppstoreHit;
import org.oasis_eu.spring.kernel.model.instance.CreateInstanceRequest;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.oasis_eu.spring.kernel.service.UserInfoService;
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
    private UserDirectory userDirectory;

    @Autowired
    private MessageSource messageSource;

    public List<AppstoreHit> getAll(List<Audience> targetAudiences) {
        Set<String> subscriptions = subscriptionStore.findByUserId(userInfoHelper.currentUser().getUserId()).stream().flatMap(s -> Arrays.asList(s.getCatalogId(), catalogStore.find(s.getCatalogId()).getParentId()).stream()).collect(Collectors.toSet());

        return catalogStore.findAllVisible(targetAudiences).stream()
                .map(c -> new AppstoreHit(RequestContextUtils.getLocale(request), c, organizationStore.find(c.getProviderId()).getName(),
                        subscriptions.contains(c.getId()) ? AcquisitionStatus.INSTALLED : AcquisitionStatus.AVAILABLE))
                .collect(Collectors.toList());
    }

    public AppInfo getInfo(String appId) {
        Locale locale = RequestContextUtils.getLocale(request);

        CatalogEntry entry = catalogStore.find(appId);

        return new AppInfo(appId, entry.getName(locale), entry.getDescription(locale), entry.getPaymentOption().equals(PaymentOption.FREE) ? messageSource.getMessage("store.it_is_free", new Object[0], locale) : messageSource.getMessage("store.it_requires_payment", new Object[0], locale));
    }

    public void buy(String appId) {
        AppInstance instanceRequest = new AppInstance();

//        instanceRequest.setProviderId(userDirectory.getMemberships(userInfoHelper.currentUser().getUserId()).get(0).getOrganizationId());
          instanceRequest.setProviderId(userInfoHelper.currentUser().getOrganizationId());

        catalogStore.instantiate(appId, instanceRequest);
    }
}
