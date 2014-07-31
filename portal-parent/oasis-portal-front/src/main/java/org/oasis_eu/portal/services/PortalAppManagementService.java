package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.mongo.dao.ApplicationInstanceRepository;
import org.oasis_eu.portal.core.mongo.model.temp.ApplicationInstanceRegistration;
import org.oasis_eu.portal.model.appsmanagement.AvailableInstance;
import org.oasis_eu.portal.model.appsmanagement.AvailableService;
import org.oasis_eu.portal.model.appsmanagement.SubscriptionStatus;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<AvailableInstance> getMyInstances() {

        String userId = userInfoService.currentUser().getUserId();

        List<AvailableInstance> instances = applicationInstanceRepository.findByUserId(userId)
                .stream()
                .map(this::fetchInstance)
                .collect(Collectors.toList());

        Set<String> subscribedServices = subscriptionStore.findByUserId(userId).stream().map(Subscription::getServiceId).collect(Collectors.toSet());

        instances.forEach(i -> i.getServices().forEach(as -> as.setSubscriptionStatus(subscribedServices.contains(as.getService().getId()) ? SubscriptionStatus.SUBSCRIBED : SubscriptionStatus.NOT_SUBSCRIBED)));

        return instances;

    }

    private AvailableInstance fetchInstance(ApplicationInstanceRegistration registration) {

        return new AvailableInstance().setApplicationInstanceRegistration(registration)
                .setApplicationInstance(catalogStore.findApplicationInstance(registration.getInstanceId()))
                .setServices(catalogStore.findServicesOfInstance(registration.getInstanceId()).stream().map(this::fetchService).collect(Collectors.toList()));
    }

    private AvailableService fetchService(CatalogEntry service) {
        return new AvailableService().setService(service);
    }
}
