package org.oasis_eu.portal.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.oasis_eu.portal.core.dao.ApplicationInstanceStore;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.dao.InstanceACLStore;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.model.subscription.SubscriptionType;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.model.appsmanagement.Authority;
import org.oasis_eu.portal.model.appsmanagement.MyAppsInstance;
import org.oasis_eu.portal.model.appsmanagement.MyAppsService;
import org.oasis_eu.portal.model.appsmanagement.User;
import org.oasis_eu.portal.model.appstore.AppInfo;
import org.oasis_eu.spring.kernel.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * User: schambon
 * Date: 7/29/14
 */
@Service
public class PortalAppManagementService {

	private static final Logger logger = LoggerFactory.getLogger(PortalAppstoreService.class);

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
				.filter(instance -> ! ApplicationInstance.InstantiationStatus.PENDING.equals(instance.getStatus()))
				.map(i -> fetchInstance(i, fetchServices))
				.filter(i -> i!= null) // skip if application Forbidden (else #208 Catalog not displayed), deleted...
				.collect(Collectors.toList());
	}

	private List<MyAppsInstance> getOrganizationInstances(Authority orgAuthority, boolean fetchServices) {
		return applicationInstanceStore.findByOrganizationId(orgAuthority.getId())
				.stream()
				.filter(instance -> !ApplicationInstance.InstantiationStatus.PENDING.equals(instance.getStatus()))
				.map(i -> fetchInstance(i, fetchServices)) // skip if application Forbidden (else #208 Catalog not displayed), deleted...
				.filter(i -> i!= null)
				.collect(Collectors.toList());

	}

	/**
	 *
	 * @param instance
	 * @param fetchServices
	 * @return null if can't find application : Forbidden (else #208 Catalog not displayed), deleted...
	 */
	private MyAppsInstance fetchInstance(ApplicationInstance instance, boolean fetchServices) {

		logger.debug("Fetching instance data for {}", instance);

		CatalogEntry entry = catalogStore.findApplication(instance.getApplicationId());
		if (entry == null) {
			return null; // Forbidden (else #208 Catalog not displayed), deleted...
		}
		AppInfo appInfo = new AppInfo(entry.getId(),
				entry.getName(RequestContextUtils.getLocale(request)),
				entry.getDescription(RequestContextUtils.getLocale(request)),
				null,
				entry.getType(),
				entry.getIcon(RequestContextUtils.getLocale(request)));

		MyAppsInstance uiInstance = fillUIInstance(
				new MyAppsInstance().setApplicationInstance(instance).setApplication(appInfo) );

		if (fetchServices)
			uiInstance = uiInstance.setServices(catalogStore.findServicesOfInstance(instance.getInstanceId()).stream()
					.map(this::fetchService).collect(Collectors.toList()));

		return uiInstance;
	}

	private MyAppsInstance fillUIInstance(MyAppsInstance uiInstance) {
		ApplicationInstance instance = uiInstance.getApplicationInstance();
		if (instance.getStatusChanged() != null) {
			Instant deletionPlanned = new DateTime(instance.getStatusChanged()) //TODO check if computeDeletionPlanned() in NetworkService is required here
				.plusDays(applicationInstanceDaysTillDeletedFromTrash).toInstant();
			uiInstance.setDeletionPlanned(deletionPlanned);
		}
		if (instance.getStatusChangeRequesterId() != null) {
			uiInstance.setStatusChangeRequesterLabel(networkService.getUserName(instance.getStatusChangeRequesterId(), null)); // TODO protected ?? then from membership
		}
		return uiInstance;
	}


	private MyAppsService fetchService(CatalogEntry service) {

		logger.debug("Fetching service data for {}", service);

		return new MyAppsService()
				.setService(service)
				.setName(service.getName(RequestContextUtils.getLocale(request)))
				.setIconUrl(imageService.getImageForURL(service.getDefaultIcon(), ImageFormat.PNG_64BY64, false));
	}

	public MyAppsService getService(String serviceId) {

		return fetchService(catalogStore.findService(serviceId));

	}

	public CatalogEntry updateService(String serviceId, CatalogEntry entry) {
		CatalogEntry catalogEntry = catalogStore.findService(serviceId);
		ApplicationInstance appInstance = catalogStore.findApplicationInstance(catalogEntry.getInstanceId());
		if ( !networkService.userIsAdminOrPersonalAppInstance(appInstance) ) {
			// let it with the default forbidden error message
			throw new ForbiddenException();
		}
		return catalogStore.fetchAndUpdateService(serviceId, entry);
	}

	/**
	 *
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
	 * @param serviceId
	 * @param usersToSubscribe (including some that are app_admin)
	 */
	public void updateSubscriptions(String serviceId, Set<String> usersToSubscribe) {
		if (!networkService.userIsAdmin(catalogStore.findApplicationInstance(catalogStore.findService(serviceId).getInstanceId()).getProviderId())) {
			throw new AccessDeniedException("Unauthorized access");
		}


		Set<String> existing = getSubscribedUsersOfService(serviceId).stream().map(User::getUserid).collect(Collectors.toSet());

		// which ones must we add?
		subscribeUsers(usersToSubscribe.stream().filter(s -> ! existing.contains(s)).collect(Collectors.toSet()), serviceId);
		// which ones must we remove?
		unsubscribeUsers(existing.stream().filter(s -> ! usersToSubscribe.contains(s)).collect(Collectors.toSet()), serviceId);
	}

	public void subscribeUsers(Set<String> users, String serviceId) {
		users.forEach(u -> {
			Subscription s = new Subscription();
			s.setSubscriptionType(SubscriptionType.ORGANIZATION);
			s.setServiceId(serviceId);
			s.setUserId(u);

			subscriptionStore.create(u, s);
		});
	}

	public void unsubscribeUsers(Set<String> users, String serviceId) {
		users.forEach(u -> subscriptionStore.unsubscribe(u, serviceId, SubscriptionType.ORGANIZATION));
	}

	/**
	 * used by MyAppsAJAXServices.getUsersForInstance() which is used by UI UserPickers
	 * to load app users (with !appAdmin) or to query service users (with appAdmin)
	 * @param instanceId
	 * @param appAdmin
	 * @return users i.e. app_user (including if app_admin see #157)
	 */
	public List<User> getAppUsers(String instanceId, boolean appAdmin) {
		return instanceACLStore.getACL(instanceId)
				.stream()
				.filter(ace -> ace.isAppUser() || appAdmin && ace.isAppAdmin()) // #157 Delete and re-add a service icon to my desk K#90
				.map(ace -> new User(ace.getUserId(), ace.getUserName(), false))
				.collect(Collectors.toList());
	}

	public void saveAppUsers(String instanceId, List<User> users) {
		if (!networkService.userIsAdmin(catalogStore.findApplicationInstance(instanceId).getProviderId())) {
			throw new AccessDeniedException("Unauthorized access");
		}

		instanceACLStore.saveACL(instanceId, users);
	}

	/**
	 * for (un)trash
	 * @param instance
	 * @return
	 */
	public String setInstanceStatus(MyAppsInstance uiInstance) {
		ApplicationInstance existingInstance = catalogStore.findApplicationInstance(uiInstance.getId());
		if (!networkService.userIsAdminOrPersonalAppInstance(existingInstance)) {
			throw new AccessDeniedException("Unauthorized access");
		}

		ApplicationInstance instance = uiInstance.getApplicationInstance();
		boolean statusHasChanged = instance.getStatus() == null ||  existingInstance.getStatus() == null || !(instance.getStatus().equals( existingInstance.getStatus()));
		if (statusHasChanged) {
			return catalogStore.setInstanceStatus(instance.getInstanceId(), instance.getStatus());
		}
		return null;
	}

}
