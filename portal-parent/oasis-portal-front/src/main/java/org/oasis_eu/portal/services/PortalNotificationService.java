package org.oasis_eu.portal.services;

import com.google.common.base.Strings;
import org.joda.time.format.DateTimeFormat;
import org.markdown4j.Markdown4jProcessor;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.model.UserNotification;
import org.oasis_eu.portal.model.dashboard.AppNotificationData;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Service
public class PortalNotificationService {

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
        return (int) notificationService.getNotifications(userInfoHelper.currentUser().getUserId(), NotificationStatus.UNREAD)
                .stream()
                .filter(n -> n.getStatus().equals(NotificationStatus.UNREAD))
                .count();
    }

    public List<UserNotification> getNotifications(Locale locale) {
        if (!notificationsEnabled) {
            return Collections.emptyList();
        }
        return notificationService.getNotifications(userInfoHelper.currentUser().getUserId(), NotificationStatus.UNREAD)
                .stream()
                .map(n -> {
                    UserNotification notif = new UserNotification();
                    CatalogEntry service = null;
                    if (n.getServiceId() != null) {

                        service = catalogStore.findService(n.getServiceId());
                        notif.setAppName(service != null ? service.getName(locale) : "");
                    }
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

                    notif.setServiceId(n.getServiceId());

                    return notif;
                })
                .sorted((n1, n2) -> n1.getDate().isAfter(n2.getDate()) ? -1 : 1)
                .collect(Collectors.toList());
    }

    public List<AppNotificationData> getAppNotificationCounts(List<String> serviceIds) {
        List<UserNotification> userNotifications = getNotifications();

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

        return getNotifications().stream()
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

    public List<UserNotification> getNotifications() {
        return getNotifications(RequestContextUtils.getLocale(request));
    }

    public void archive(String notificationId) {
        if (!notificationsEnabled) {
            return;
        }
        notificationService.setMessageStatus(userInfoHelper.currentUser().getUserId(), Arrays.asList(notificationId), NotificationStatus.READ);
    }


}
