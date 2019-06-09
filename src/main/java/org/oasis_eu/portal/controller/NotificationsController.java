package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.model.notifications.UserNotificationResponse;
import org.oasis_eu.portal.services.NotificationService;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/my/api/notifications")
public class NotificationsController {

    private final NotificationService notificationService;

    public NotificationsController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = {"", "/"})
    public UserNotificationResponse getNotifications(@RequestParam(value = "status", required = false, defaultValue = "UNREAD") NotificationStatus status) {
        return notificationService.getNotifications(status);
    }

    @DeleteMapping(value = "/{notificationId}")
    public void archive(@PathVariable String notificationId) {
        notificationService.archive(notificationId);
    }

    @GetMapping(value = "summary")
    public Integer getNotificationData(HttpServletRequest request) {
        return notificationService.countNotifications();
    }
}
