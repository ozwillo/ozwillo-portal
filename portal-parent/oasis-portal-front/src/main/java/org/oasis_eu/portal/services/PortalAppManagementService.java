package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.mongo.dao.ApplicationInstanceRepository;
import org.oasis_eu.portal.core.mongo.model.temp.ApplicationInstanceRegistration;
import org.oasis_eu.portal.model.appsmanagement.MyAppsInstance;
import org.oasis_eu.portal.model.appsmanagement.MyAppsService;
import org.oasis_eu.portal.model.appsmanagement.SubscriptionStatus;
import org.oasis_eu.portal.model.appstore.AppInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 7/29/14
 */
@Service
public class PortalAppManagementService {

    @Autowired
    private ApplicationInstanceRepository applicationInstanceRepository;

    @Autowired
    private CatalogStore catalogStore;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private HttpServletRequest request;

    public List<MyAppsInstance> getMyInstances() {

        String userId = userInfoService.currentUser().getUserId();

        List<MyAppsInstance> instances = applicationInstanceRepository.findByUserId(userId)
                .stream()
                .map(this::fetchInstance)
                .collect(Collectors.toList());

        Set<String> subscribedServices = subscriptionStore.findByUserId(userId).stream().map(Subscription::getServiceId).collect(Collectors.toSet());

        instances.forEach(i -> i.getServices().forEach(as -> {
            as.setSubscriptionStatus(subscribedServices.contains(as.getService().getId()) ? SubscriptionStatus.SUBSCRIBED : SubscriptionStatus.NOT_SUBSCRIBED);
            as.setName(as.getService().getName(RequestContextUtils.getLocale(request)));
        }));

        return instances;

    }

    private MyAppsInstance fetchInstance(ApplicationInstanceRegistration registration) {

        ApplicationInstance applicationInstance = catalogStore.findApplicationInstance(registration.getInstanceId());
        CatalogEntry entry = catalogStore.findApplication(applicationInstance.getApplicationId());
        AppInfo appInfo = new AppInfo(entry.getId(), entry.getName(RequestContextUtils.getLocale(request)), entry.getDescription(RequestContextUtils.getLocale(request)), null, entry.getType());



        return new MyAppsInstance().setApplicationInstanceRegistration(registration)
                .setApplicationInstance(applicationInstance)
                .setApplication(appInfo)
                .setServices(catalogStore.findServicesOfInstance(registration.getInstanceId()).stream().map(this::fetchService).collect(Collectors.toList()));
    }

    private MyAppsService fetchService(CatalogEntry service) {
        return new MyAppsService().setService(service);
    }
}
