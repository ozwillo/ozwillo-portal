package org.oasis_eu.portal.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.format.DateTimeFormat;
import org.markdown4j.Markdown4jProcessor;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.model.dashboard.AppNotificationData;
import org.oasis_eu.portal.model.notifications.NotifApp;
import org.oasis_eu.portal.model.notifications.UserNotification;
import org.oasis_eu.portal.model.notifications.UserNotificationResponse;
import org.oasis_eu.spring.kernel.model.InboundNotification;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.oasis_eu.spring.kernel.service.NotificationService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.google.common.base.Strings;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Service
public class PortalNotificationService {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(PortalNotificationService.class);

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private CatalogStore catalogStore;

	@Autowired
	private UserInfoService userInfoHelper;

	@Autowired
	private HttpServletRequest request;

	@Value("${application.notificationsEnabled:true}")
	private boolean notificationsEnabled;

	@Value("${application.devmode:false}")
	private boolean devmode;

	@Autowired
	private MessageSource messageSource;

	public int countNotifications() {
		if (!notificationsEnabled) {
			return 0;
		}
		// TODO: GET from kernel (not yet implemented at 18/June/2015) the actual count of {status: READ, UNREAD, ALL} message.
		// NB. In case the user has +300 notifications, it will be fetch ALL the notification content each time,
		// this to be filtered and counted at the end, which implies use of unnecessary networking/processing tasks
		return (int) notificationService.getNotifications(userInfoHelper.currentUser().getUserId(), NotificationStatus.UNREAD)
				.stream()
				.filter(n -> n.getStatus().equals(NotificationStatus.UNREAD))
				.count();
	}

	public UserNotificationResponse getNotifications(Locale locale, NotificationStatus status) {
		if (!notificationsEnabled) {
			return new UserNotificationResponse();
		}
		List<InboundNotification> notifications = notificationService.getNotifications(userInfoHelper.currentUser().getUserId(), NotificationStatus.ANY);

		List<NotifApp> notifApps = notifications
				.stream()
				.map(n -> {
					CatalogEntry service = null;
					if (n.getServiceId() != null) {
						service = catalogStore.findService(n.getServiceId());
						if (service == null) {
							return null;
						} else {
							return new NotifApp(service.getId(), service.getName(locale));
						}
					} else if (n.getInstanceId() != null) {
						ApplicationInstance instance = catalogStore.findApplicationInstance(n.getInstanceId());
						if (instance == null) {
							return null;
						} else {
							return new NotifApp(instance.getApplicationId(), catalogStore.findApplication(instance.getApplicationId()).getName(locale));
						}
					} else return null;
				})
				.filter(napp -> napp != null)
				.distinct()
				.collect(Collectors.toList());


		List<UserNotification> notifs = extractNotifications(locale, status, notifications);

		return new UserNotificationResponse(notifs, notifApps);
	}

	/**
	 * Specifically get unread notifications only (don't query the kernel for other stuff)
	 *
	 * @return
	 */
	public List<UserNotification> getUnreadNotifications() {
		return extractNotifications(RequestContextUtils.getLocale(request),
				NotificationStatus.UNREAD, notificationService.getNotifications(userInfoHelper.currentUser().getUserId(), NotificationStatus.UNREAD));
	}

	public List<UserNotification> extractNotifications(Locale locale, NotificationStatus status, List<InboundNotification> notifications) {
		return notifications
				.stream()
				.filter(n -> NotificationStatus.ANY.equals(status) || status.equals(n.getStatus()))
				.map(n -> {
					UserNotification notif = new UserNotification();
					CatalogEntry service = null;

					if (n.getServiceId() != null) {
						service = catalogStore.findService(n.getServiceId());
						if (service == null) {
							return null; // skip deleted service, probable (?) companion case to #179 Bug with notifications referring destroyed app instances
							// TODO LATER keep service but with "deleted" flag so it doesn't happen (rather than auto deleting this portal data)
						}
						notif.setAppName(service != null ? service.getName(locale) : "");
						notif.setServiceId(service.getId());

					} else if (n.getInstanceId() != null) {
						ApplicationInstance instance = catalogStore.findApplicationInstance(n.getInstanceId());
						if (instance == null) {
							// case of #179 Bug with notifications referring destroyed app instances or #206 500 on portal notification api
							// LATER we could keep app instance with a "deleted" flag so it doesn't happen (rather than auto deleting this portal data),
							// but this wouldn't address the Forbidden case)
							if (!devmode) {
								return null; // skip deleted or (newly) Forbidden app instance (rather than displaying no name)
							} else {
								notif.setAppName("Application with deleted or forbidden instance"); // to help debug
								notif.setServiceId("");
							}
						} else {
							CatalogEntry application = catalogStore.findApplication(instance.getApplicationId());
							notif.setAppName(application != null ? application.getName(locale) : "");
							notif.setServiceId(application.getId());
						}
					}

					//notif.setDate( n.getTime() == null ? new Instant() : n.getTime()); // TODO workaround, but kernel should always provide a date
					notif.setDate(n.getTime());
					notif.setDateText(DateTimeFormat.forPattern(DateTimeFormat.patternForStyle("MS", locale)).print(n.getTime()));

					notif.setFormattedText(getFormattedText(n));
					notif.setId(n.getId());

					if (Strings.isNullOrEmpty(n.getActionUri())) {
						if (service != null) {
							notif.setUrl(service.getNotificationUrl());
						}
					} else {
						notif.setUrl(n.getActionUri());
					}

					if (Strings.isNullOrEmpty(n.getActionLabel())) {
						notif.setActionText(messageSource.getMessage("notif.manage", new Object[0], locale));
					} else {
						notif.setActionText(n.getActionLabel());
					}

					notif.setStatus(n.getStatus());

					return notif;
				})
				.filter(n -> n != null) // case of deleted or Forbidden app instance, see above
				.sorted((n1, n2) -> n1.getDate() != null && (n2.getDate() == null // some old notif, but would mean "now" for joda time
						|| n1.getDate().isAfter(n2.getDate())) ? -1 : 1)
				.collect(Collectors.toList());
	}

	// TODO: Method is Not used, verify if is required, otherwise remove it
	public List<AppNotificationData> getAppNotificationCounts(List<String> serviceIds) {
		List<UserNotification> userNotifications = getUnreadNotifications();


		/*
		Equivalent SQL:
		SELECT serviceId, count(*) FROM userNotifications WHERE serviceId IN serviceIds GROUP BY serviceId
		(then bump into a List<AppNotificationData>)
		 */
		return userNotifications.stream()
				.filter(un -> serviceIds.contains(un.getServiceId()))
				.collect(Collectors.groupingBy(notif -> notif.getServiceId(), Collectors.reducing(0, n -> 1, Integer::sum)))
				.entrySet().stream()
				.map(entry -> new AppNotificationData(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}


	public Map<String, Integer> getAppNotificationCounts() {

		return getUnreadNotifications().stream()
				.filter(notif -> notif.getServiceId() != null)
				.collect(Collectors.groupingBy(notif -> notif.getServiceId(), Collectors.reducing(0, n -> 1, Integer::sum)));

	}

	private String getFormattedText(InboundNotification n) {
		String formattedText;
		String message = n.getMessage().replaceAll("[<>]", "");

		try {
			formattedText = new Markdown4jProcessor().process(message);
		} catch (IOException e) {
			formattedText = message;
		}
		return formattedText;
	}

	public UserNotificationResponse getNotifications(NotificationStatus status) {
		return getNotifications(RequestContextUtils.getLocale(request), status);
	}

	public void archive(String notificationId) {
		if (!notificationsEnabled) {
			return;
		}
		notificationService.setMessageStatus(userInfoHelper.currentUser().getUserId(), Arrays.asList(notificationId), NotificationStatus.READ);
	}


}
