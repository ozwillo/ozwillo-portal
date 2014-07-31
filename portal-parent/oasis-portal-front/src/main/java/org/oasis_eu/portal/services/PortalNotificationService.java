package org.oasis_eu.portal.services;

import org.joda.time.Instant;
import org.markdown4j.Markdown4jProcessor;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.model.UserNotification;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.oasis_eu.spring.kernel.service.NotificationService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    public Map<String, Integer> getAppNotifications(List<String> applicationIds) {
        return notificationService.getNotifications(userInfoHelper.currentUser().getUserId())
                .stream()
                .filter(n -> n.getApplicationId() != null && applicationIds.contains(n.getApplicationId()) && n.getStatus().equals(NotificationStatus.UNREAD))
                .collect(Collectors.groupingBy(n -> n.getApplicationId(), Collectors.reducing(0, n -> 1, Integer::sum)));
    }

    public int countNotifications() {
        return (int) notificationService.getNotifications(userInfoHelper.currentUser().getUserId())
                .stream()
                .filter(n -> n.getStatus().equals(NotificationStatus.UNREAD))
                .count();
    }

    public long countAppNotifications(String appId) {
        return notificationService.getAppNotifications(userInfoHelper.currentUser().getUserId(), appId)
                .stream()
                .filter(n -> n.getStatus().equals(NotificationStatus.UNREAD))
                .count();

    }

    public List<UserNotification> getNotifications(Locale locale) {
        return notificationService.getNotifications(userInfoHelper.currentUser().getUserId())
                .stream()
                .filter(n -> n.getStatus().equals(NotificationStatus.UNREAD))
                .map(n -> {
                    UserNotification notif = new UserNotification();
                    CatalogEntry application = store.find(n.getApplicationId());
                    notif.setAppName(application != null ? application.getName(locale) : "");
                    notif.setDate(new Instant().withMillis(n.getTime()));
                    try {
                        notif.setFormattedText(new Markdown4jProcessor().process(n.getMessage()));
                    } catch (IOException e) {
                        logger.error("Cannot render content", e);
                        notif.setFormattedText(n.getMessage());
                    }
                    return notif;
                })
                .sorted((n1, n2) -> n1.getDate().isBefore(n2.getDate()) ? -1 : 1)
                .collect(Collectors.toList());

    }
}
