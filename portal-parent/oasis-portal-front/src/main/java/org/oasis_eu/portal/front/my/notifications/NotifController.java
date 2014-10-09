package org.oasis_eu.portal.front.my.notifications;

import org.oasis_eu.portal.model.UserNotification;
import org.oasis_eu.portal.services.PortalNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User: schambon
 * Date: 10/9/14
 */
@RestController
@RequestMapping("/my/api/notif")
public class NotifController {

    @Autowired
    private PortalNotificationService notificationService;

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public List<UserNotification> getNotifications() {
        return notificationService.getNotifications();
    }

    @RequestMapping(value = "/{notificationId}", method = RequestMethod.DELETE)
    public void archive(@PathVariable String notificationId) {
        notificationService.archive(notificationId);
    }
}
