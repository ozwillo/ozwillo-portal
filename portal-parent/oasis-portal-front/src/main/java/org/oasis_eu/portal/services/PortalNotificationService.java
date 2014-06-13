package org.oasis_eu.portal.services;

import org.joda.time.Instant;
import org.markdown4j.Markdown4jProcessor;
import org.oasis_eu.portal.core.dao.ApplicationStore;
import org.oasis_eu.portal.core.model.appstore.Application;
import org.oasis_eu.portal.core.services.cms.ContentRenderingService;
import org.oasis_eu.portal.model.UserNotification;
import org.oasis_eu.spring.kernel.model.InboundNotification;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.oasis_eu.spring.kernel.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
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
    private ApplicationStore store;

    @Autowired
    private UserInfoHelper userInfoHelper;

    public long countNotifications() {
        return notificationService.getNotifications(userInfoHelper.currentUser().getUserId())
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
                    Application application = store.find(n.getApplicationId());
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
