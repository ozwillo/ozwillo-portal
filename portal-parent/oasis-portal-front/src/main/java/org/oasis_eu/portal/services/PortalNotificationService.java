package org.oasis_eu.portal.services;

import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.oasis_eu.spring.kernel.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: schambon
 * Date: 6/13/14
 */
@Service
public class PortalNotificationService {

    @Autowired
    private NotificationService notificationService;

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

}
