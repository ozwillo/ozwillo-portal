package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.model.notifications.UserNotificationResponse;
import org.oasis_eu.portal.services.NotificationService;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * User: schambon
 * Date: 8/14/14
 */
@RestController
@RequestMapping("/my/api/notifications")
public class NotificationsController {

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    private NotificationService notificationService;

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public UserNotificationResponse getNotifications(@RequestParam(value = "status", required = false, defaultValue = "UNREAD") NotificationStatus status) {
        return notificationService.getNotifications(status);
    }

    @RequestMapping(value = "/{notificationId}", method = RequestMethod.DELETE)
    public void archive(@PathVariable String notificationId) {
        notificationService.archive(notificationId);
    }

    @RequestMapping(value = "summary", method = RequestMethod.GET)
    @ResponseBody
    public Integer getNotificationData(HttpServletRequest request) {
        return notificationService.countNotifications();
    }

}
