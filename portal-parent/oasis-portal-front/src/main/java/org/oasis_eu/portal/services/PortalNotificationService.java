package org.oasis_eu.portal.services;

import com.google.common.base.Strings;
import org.joda.time.Instant;
import org.markdown4j.Markdown4jProcessor;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.model.UserNotification;
import org.oasis_eu.spring.kernel.model.InboundNotification;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.oasis_eu.spring.kernel.service.NotificationService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.DateFormat;
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
    private CatalogStore store;

    @Autowired
    private UserInfoService userInfoHelper;

    @Autowired
    private HttpServletRequest request;

    @Value("${application.notificationsEnabled:true}")
    private boolean notificationsEnabled;

    public Map<String, Integer> getServiceNotifications(List<String> serviceIds) {
        if (!notificationsEnabled) {
            return Collections.emptyMap();
        }
        return notificationService.getNotifications(userInfoHelper.currentUser().getUserId())
                .stream()
                .filter(n -> n.getApplicationId() != null && serviceIds.contains(n.getApplicationId()) && n.getStatus().equals(NotificationStatus.UNREAD))
                .collect(Collectors.groupingBy(n -> n.getApplicationId(), Collectors.reducing(0, n -> 1, Integer::sum)));
    }

    public int countNotifications() {
        if (!notificationsEnabled) {
            return 0;
        }
        return (int) notificationService.getNotifications(userInfoHelper.currentUser().getUserId())
                .stream()
                .filter(n -> n.getStatus().equals(NotificationStatus.UNREAD))
                .count();
    }

    public long countAppNotifications(String appId) {
        if (!notificationsEnabled) {
            return 0;
        }
        return notificationService.getAppNotifications(userInfoHelper.currentUser().getUserId(), appId)
                .stream()
                .filter(n -> n.getStatus().equals(NotificationStatus.UNREAD))
                .count();

    }

    public List<UserNotification> getNotifications(Locale locale) {
        if (!notificationsEnabled) {
            return Collections.emptyList();
        }
        return notificationService.getNotifications(userInfoHelper.currentUser().getUserId())
                .stream()
                .filter(n -> n.getStatus().equals(NotificationStatus.UNREAD))
                .map(n -> {
                    UserNotification notif = new UserNotification();
                    CatalogEntry service = null;
                    if (n.getApplicationId() != null) {

                        service = store.findService(n.getApplicationId());
                        notif.setAppName(service != null ? service.getName(locale) : "");
                    }
                    notif.setDate(new Instant().withMillis(n.getTime()));
                    notif.setDateText(DateFormat.getDateInstance(DateFormat.SHORT, locale).format(new Date(n.getTime())));

                    String formattedText = getFormattedText(n);

                    notif.setFormattedText(formattedText);
                    notif.setId(n.getId());

                    if (Strings.isNullOrEmpty(n.getData())) {
                        if (service != null) {
                            notif.setUrl(service.getNotificationUrl());
                        }
                    } else {
                        notif.setUrl(n.getData());
                    }
                    return notif;
                })
                .sorted((n1, n2) -> n1.getDate().isBefore(n2.getDate()) ? -1 : 1)
                .collect(Collectors.toList());
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
