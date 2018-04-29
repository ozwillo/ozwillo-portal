package org.oasis_eu.portal.services;

import com.google.common.base.Strings;
import org.markdown4j.Markdown4jProcessor;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.catalog.ServiceEntry;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private MessageSource messageSource;

    public int countNotifications() {
        if (!notificationsEnabled) {
            return 0;
        }
        // TODO NB. In case the user has +300 notifications, it will be fetch ALL the notification content each time,
        // this to be filtered and counted at the end, which implies use of unnecessary networking/processing tasks
        return notificationService.getNotifications(userInfoHelper.currentUser().getUserId(), NotificationStatus.UNREAD)
                .size();
    }

    public UserNotificationResponse getNotifications(NotificationStatus status) {
        return getNotifications(RequestContextUtils.getLocale(request), status);
    }

    private UserNotificationResponse getNotifications(Locale locale, NotificationStatus status) {
        if (!notificationsEnabled) {
            return new UserNotificationResponse();
        }

        List<InboundNotification> notifications =
            notificationService.getNotifications(userInfoHelper.currentUser().getUserId(), status);

        List<UserNotification> notifs = extractNotifications(locale, status, notifications);

        List<NotifApp> notifApps = notifs.stream()
            .filter(userNotification -> userNotification.getApplicationId() != null)
            .map(userNotification -> new NotifApp(userNotification.getApplicationId(), userNotification.getAppName()))
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        return new UserNotificationResponse(notifs, notifApps);
    }

    private List<UserNotification> extractNotifications(Locale locale, NotificationStatus status, List<InboundNotification> notifications) {
        return notifications
            .stream()
            .filter(n -> NotificationStatus.ANY.equals(status) || status.equals(n.getStatus()))
            .map(n -> {
                UserNotification notif = new UserNotification();
                ServiceEntry serviceEntry = null;

                if (n.getServiceId() != null) {
                    serviceEntry = catalogStore.findService(n.getServiceId());
                    if (serviceEntry == null) {
                        logger.warn("Service {} not found or no longer authorized", n.getServiceId());
                        archive(n.getId());
                        return null; // skip deleted service, probable (?) companion case to #179 Bug with notifications referring destroyed app instances
                    }
                    notif.setAppName(serviceEntry.getName(locale));
                    notif.setServiceId(n.getServiceId());
                    notif.setApplicationId(serviceEntry.getId());

                } else if (n.getInstanceId() != null) {
                    ApplicationInstance instance = catalogStore.findApplicationInstanceOrNull(n.getInstanceId());
                    if (instance == null) {
                        // case of #179 Bug with notifications referring destroyed app instances or #206 500 on portal notification api
                        logger.warn("Instance {} not found or no longer authorized", n.getInstanceId());
                        archive(n.getId());
                        return null; // skip deleted or (newly) Forbidden app instance (rather than displaying no name)
                    } else {
                        CatalogEntry application = catalogStore.findApplication(instance.getApplicationId());
                        notif.setAppName(application.getName(locale));
                        notif.setServiceId(n.getInstanceId());
                        notif.setApplicationId(application.getId());
                    }
                }

                notif.setDate(n.getTime());
                notif.setDateText(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(LocalDateTime.ofInstant(n.getTime(), ZoneOffset.UTC)));

                notif.setFormattedText(getFormattedText(n, locale));
                notif.setId(n.getId());

                if (Strings.isNullOrEmpty(n.getActionUri())) {
                    if (serviceEntry != null) {
                        notif.setUrl(serviceEntry.getNotificationUrl());
                    }
                } else {
                    notif.setUrl(n.getActionUri());
                }

                if (Strings.isNullOrEmpty(n.getActionLabel())) {
                    notif.setActionText(messageSource.getMessage("notif.manage", new Object[0], locale));
                } else {
                    notif.setActionText(n.getActionLabel(locale));
                }

                notif.setStatus(n.getStatus());

                return notif;
            })
            .filter(Objects::nonNull) // case of deleted or Forbidden app instance, see above
            .sorted((n1, n2) -> n1.getDate() != null && (n2.getDate() == null // some old notif, but would mean "now" for joda time
                || n1.getDate().isAfter(n2.getDate())) ? -1 : 1)
            .collect(Collectors.toList());
    }

    public Map<String, Integer> getAppNotificationCounts() {

        List<InboundNotification> inboundNotifications =
            notificationService.getNotifications(userInfoHelper.currentUser().getUserId(), NotificationStatus.UNREAD)
                .stream()
                .filter(inboundNotification -> inboundNotification.getServiceId() != null || inboundNotification.getInstanceId() != null)
                .collect(Collectors.toList());

        List<UserNotification> userNotifications = extractNotifications(RequestContextUtils.getLocale(request),
            NotificationStatus.UNREAD, inboundNotifications);

        return userNotifications.stream()
            .filter(userNotification -> !Strings.isNullOrEmpty(userNotification.getServiceId()))
            .collect(Collectors.groupingBy(UserNotification::getServiceId, Collectors.reducing(0, n -> 1, Integer::sum)));
    }

    private static String getFormattedText(InboundNotification notification, Locale locale) {
        String formattedText;
        String message = notification.getMessage(locale).replaceAll("[<>]", "");

        try {
            formattedText = new Markdown4jProcessor().process(message);
        } catch (IOException ignore) {
            formattedText = message;
        }
        return formattedText;
    }

    public void archive(String notificationId) {
        if (!notificationsEnabled) {
            return;
        }
        notificationService.setMessageStatus(userInfoHelper.currentUser().getUserId(),
            Collections.singletonList(notificationId), NotificationStatus.READ);
    }
}
