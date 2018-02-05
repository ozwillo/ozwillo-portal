package org.oasis_eu.portal.services;

import org.oasis_eu.portal.core.dao.ApplicationInstanceStore;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.InstanceACLStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.catalog.ServiceEntry;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.model.app.service.InstanceService;
import org.oasis_eu.portal.model.authority.Authority;
import org.oasis_eu.portal.model.app.instance.MyAppsInstance;
import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.portal.services.kernel.UserProfileService;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 7/29/14
 */
@Service
public class ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    @Value("${application.applicationInstanceDaysTillDeletedFromTrash:7}")
    private int applicationInstanceDaysTillDeletedFromTrash;

    @Autowired
    private CatalogStore catalogStore;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Autowired
    private ApplicationInstanceStore applicationInstanceStore;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private InstanceACLStore instanceACLStore;

    @Autowired
    private ImageService imageService;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private UserProfileService userProfileService;

    public List<MyAppsInstance> getMyInstances(Authority authority, boolean fetchServices) {
        switch (authority.getType()) {
            case INDIVIDUAL:
                return getPersonalInstances(authority, fetchServices);
            case ORGANIZATION:
                return getOrganizationInstances(authority, fetchServices);
        }

        logger.error("Should never be here - authority is neither an individual or an organization: {}", authority.getType());
        return null;
    }

    private List<MyAppsInstance> getPersonalInstances(Authority personalAuthority, boolean fetchServices) {
        return applicationInstanceStore.findByUserId(personalAuthority.getId())
            .stream()
            .sorted(Comparator.comparing(ApplicationInstance::getStatus).reversed())
            .map(i -> fetchInstance(i, fetchServices))
            .filter(i -> i != null) // skip if application Forbidden (else #208 Catalog not displayed), deleted...
            .collect(Collectors.toList());
    }

    private List<MyAppsInstance> getOrganizationInstances(Authority orgAuthority, boolean fetchServices) {
        return applicationInstanceStore.findByOrganizationId(orgAuthority.getId())
            .stream()
            .sorted(Comparator.comparing(ApplicationInstance::getStatus).reversed())
            .map(i -> fetchInstance(i, fetchServices)) // skip if application Forbidden (else #208 Catalog not displayed), deleted...
            .filter(i -> i != null)
            .collect(Collectors.toList());
    }

    private MyAppsInstance fetchInstance(ApplicationInstance instance, boolean fetchServices) {

        logger.debug("Fetching instance data for {}, instance id {}", instance.getDefaultName(), instance.getInstanceId());

        MyAppsInstance uiInstance = fillUIInstance(new MyAppsInstance(instance));

        if (fetchServices)
            uiInstance.setServices(catalogStore.findServicesOfInstance(instance.getInstanceId()).stream()
                .map(this::fetchService).collect(Collectors.toList()));

        return uiInstance;
    }

    private MyAppsInstance fillUIInstance(MyAppsInstance uiInstance) {
        ApplicationInstance instance = uiInstance.getApplicationInstance();
        if (instance.getStatusChanged() != null) {
            LocalDateTime statusChangedDate = LocalDateTime.ofInstant(instance.getStatusChanged(), ZoneOffset.UTC);
            uiInstance.setDeletionPlanned(statusChangedDate.plusDays(applicationInstanceDaysTillDeletedFromTrash).toInstant(ZoneOffset.UTC));
        }
        if (instance.getStatusChangeRequesterId() != null) {
            uiInstance.setStatusChangeRequesterLabel(userProfileService.findUserProfile(instance.getStatusChangeRequesterId()).getDisplayName()); // TODO protected ?? then from membership
        }
        return uiInstance;
    }


    private InstanceService fetchService(CatalogEntry service) {

        logger.debug("Fetching service data for {}", service);

        return new InstanceService()
            .setCatalogEntry(service)
            .setName(service.getName(RequestContextUtils.getLocale(request)))
            .setIconUrl(imageService.getImageForURL(service.getIcon(), ImageFormat.PNG_64BY64, false));
    }

    public InstanceService getService(String serviceId) {

        return fetchService(catalogStore.findService(serviceId));

    }

    public void updateService(String serviceId, ServiceEntry serviceEntry) {
        ApplicationInstance appInstance = catalogStore.findApplicationInstance(serviceEntry.getInstanceId());
        if (!networkService.userIsAdminOrPersonalAppInstance(appInstance)) {
            // let it with the default forbidden error message
            throw new ForbiddenException();
        }
        catalogStore.updateService(serviceId, serviceEntry);
    }

    /**
     * @param serviceId
     * @return users (including some that are app_admin)
     */
    public List<User> getSubscribedUsersOfService(String serviceId) {
        return subscriptionStore.findByServiceId(serviceId)
            .stream()
            .map(s -> new User(s.getUserId(), s.getUserName(), false))
            .collect(Collectors.toList());
    }


    /**
     * Used to save subscriptions but also to pushToDashboard
     * (only new subscriptions are pushed to dashboard, so to push to dashboard
     * an existing one it must be removed in a first step)
     *
     * @param serviceId
     * @param usersToSubscribe (including some that are app_admin)
     */
    public void updateSubscriptions(String serviceId, Set<String> usersToSubscribe) {
        if (!networkService.userIsAdmin(catalogStore.findApplicationInstance(catalogStore.findService(serviceId).getInstanceId()).getProviderId())) {
            throw new AccessDeniedException("Unauthorized access");
        }


        Set<String> existing = getSubscribedUsersOfService(serviceId).stream().map(User::getUserid).collect(Collectors.toSet());

        // which ones must we add?
        subscribeUsers(usersToSubscribe.stream().filter(s -> !existing.contains(s)).collect(Collectors.toSet()), serviceId);
        // which ones must we remove?
        unsubscribeUsers(existing.stream().filter(s -> !usersToSubscribe.contains(s)).collect(Collectors.toSet()), serviceId);
    }

    public void subscribeUser(String userId, String serviceId) {
        Subscription s = new Subscription();
        s.setSubscriptionType(SubscriptionType.ORGANIZATION);
        s.setServiceId(serviceId);
        s.setUserId(userId);

        subscriptionStore.create(userId, s);
    }

    public void subscribeUsers(Set<String> users, String serviceId) {
        users.forEach(u -> {
            subscribeUser(u, serviceId);
        });
    }

    public void unsubscribeUser(String userId, String serviceId) {
        subscriptionStore.unsubscribe(userId, serviceId, SubscriptionType.ORGANIZATION);
    }

    public void unsubscribeUsers(Set<String> users, String serviceId) {
        users.forEach(u -> unsubscribeUser(u, serviceId));
    }

    /**
     * used by MyAppsController.getUsersForInstance() which is used by UI UserPickers
     * to load app users (with !appAdmin) or to query service users (with appAdmin)
     *
     * @param instanceId
     * @param appAdmin
     * @return users i.e. app_user (including if app_admin see #157)
     */
    public List<User> getAppUsers(String instanceId, boolean appAdmin) {
        return instanceACLStore.getACL(instanceId)
            .stream()
            .filter(ace -> ace.isAppUser() || appAdmin && ace.isAppAdmin()) // #157 Delete and re-add a service icon to my desk K#90
            .map(ace -> new User(ace.getUserId(), null, ace.getUserName(), ace.getCreated(), false))
            .collect(Collectors.toList());
    }

    public List<User> getPendingAppUsers(String instanceId) {
        return instanceACLStore.getPendingACL(instanceId)
            .stream()
            .map(ace -> new User(null, ace.getEmail(), null, ace.getCreated(), false))
            .collect(Collectors.toList());
    }

    public List<User> getAllAppUsers(String instanceId) {
        List<User> users = getAppUsers(instanceId, false);
        users.addAll(getPendingAppUsers(instanceId));
        return users;
    }

    public void saveAppUsers(String instanceId, List<User> users) {
        if (!networkService.userIsAdmin(catalogStore.findApplicationInstance(instanceId).getProviderId())) {
            throw new AccessDeniedException("Unauthorized access");
        }

        instanceACLStore.saveACL(instanceId, users);
    }

    public void createAcl(String instanceId, User user) {
        if(user.getUserid() != null && !user.getUserid().isEmpty()){
            instanceACLStore.createACL(instanceId, user);
        } else {
            instanceACLStore.createACL(instanceId, user.getEmail());
        }

    }

    public void deleteAcl(String instanceId, User user) {
        //Delete pending ACL
        if(user.getUserid() == null || user.getUserid().isEmpty()){
            instanceACLStore.deleteACL(instanceId, user.getEmail());
            return;
        }

        //Delete ACL
        instanceACLStore.deleteACL(instanceId, user);

        //Delete user's subscriptions for services of instance
        List<ServiceEntry> instanceServices = catalogStore.findServicesOfInstance(instanceId);
        subscriptionStore.findByUserId(user.getUserid())
                .stream()
                .filter(sub -> instanceServices
                        .stream()
                        .anyMatch(s -> s.getId().equals(sub.getServiceId())))
                .forEach(sub -> subscriptionStore.unsubscribe(sub.getId()));

    }

    /**
     * for (un)trash
     */
    public String setInstanceStatus(MyAppsInstance uiInstance) {
        ApplicationInstance existingInstance = catalogStore.findApplicationInstance(uiInstance.getId());
        if (!networkService.userIsAdminOrPersonalAppInstance(existingInstance)) {
            throw new AccessDeniedException("Unauthorized access");
        }

        ApplicationInstance instance = uiInstance.getApplicationInstance();
        boolean statusHasChanged = instance.getStatus() == null || existingInstance.getStatus() == null || !(instance.getStatus().equals(existingInstance.getStatus()));
        if (statusHasChanged) {
            return catalogStore.setInstanceStatus(instance.getInstanceId(), instance.getStatus());
        }
        return null;
    }

}
