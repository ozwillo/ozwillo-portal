package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.ApplicationInstanceStore;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.model.appsmanagement.*;
import org.oasis_eu.portal.model.appstore.AppInfo;
import org.oasis_eu.spring.kernel.model.directory.UserMembership;
import org.oasis_eu.spring.kernel.service.OrganizationStore;
import org.oasis_eu.spring.kernel.service.UserDirectory;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 7/29/14
 */
@Service
public class PortalAppManagementService {

    private static final Logger logger = LoggerFactory.getLogger(PortalAppstoreService.class);

    @Autowired
    private CatalogStore catalogStore;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Autowired
    private UserDirectory userDirectory;

    @Autowired
    private ApplicationInstanceStore applicationInstanceStore;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MessageSource messageSource;


    public List<Authority> getMyAuthorities() {
        String userId = userInfoService.currentUser().getUserId();


        List<Authority> authorities = new ArrayList<>();
        authorities.add(new Authority(AuthorityType.INDIVIDUAL, messageSource.getMessage("my.apps.personal", new Object[0], RequestContextUtils.getLocale(request)), userId));

        authorities.addAll(userDirectory.getMemberships(userId)
                .stream()
                .map(this::toAuthority)
                .collect(Collectors.toList()));

        Collections.sort(authorities, (one, two) -> {
            if (one.getType().ordinal() != two.getType().ordinal()) return one.getType().ordinal() - two.getType().ordinal();
            else return one.getName().compareTo(two.getName());
        });

        return authorities;
    }

    private Authority toAuthority(UserMembership userMembership) {
        return new Authority(AuthorityType.ORGANIZATION, userMembership.getOrganizationName(), userMembership.getOrganizationId());
    }

    public List<MyAppsInstance> getMyInstances(Authority authority) {

        String userId = userInfoService.currentUser().getUserId();

        List<MyAppsInstance> instances = applicationInstanceStore.findByUserId(userId)
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

    private MyAppsInstance fetchInstance(ApplicationInstance instance) {

        logger.debug("Fetching instance data for {}", instance);

        CatalogEntry entry = catalogStore.findApplication(instance.getApplicationId());
        AppInfo appInfo = new AppInfo(entry.getId(), entry.getName(RequestContextUtils.getLocale(request)), entry.getDescription(RequestContextUtils.getLocale(request)), null, entry.getType());


        return new MyAppsInstance()
                .setApplicationInstance(instance)
                .setApplication(appInfo)
                .setServices(catalogStore.findServicesOfInstance(instance.getInstanceId()).stream().map(this::fetchService).collect(Collectors.toList()));
    }

    private MyAppsService fetchService(CatalogEntry service) {
        return new MyAppsService().setService(service);
    }
}
