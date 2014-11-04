package org.oasis_eu.portal.front.my.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.model.dashboard.AppNotificationData;
import org.oasis_eu.portal.services.PortalDashboardService;
import org.oasis_eu.portal.services.PortalNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * User: schambon
 * Date: 8/14/14
 */
@RestController
@RequestMapping("/my/api")
public class NotificationsController {

    @Autowired
    private PortalNotificationService notificationService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PortalDashboardService portalDashboardService;

    @Value("${application.notificationsEnabled:true}")
    private boolean notificationsEnabled;

    @RequestMapping(method = RequestMethod.GET, value="/notifications")
    @ResponseBody
    public NotificationData getNotificationData(HttpServletRequest request) {
        int count = notificationService.countNotifications();
        return new NotificationData(count).setNotificationsMessage(messageSource.getMessage("my.n_notifications", new Object[]{Integer.valueOf(count)}, RequestContextUtils.getLocale(request)));
    }

    @RequestMapping(method = RequestMethod.GET, value="/app-notifications/{contextId}")
    @ResponseBody
    public List<AppNotificationData> getAppNotifications(@PathVariable String contextId) {
        if (!notificationsEnabled) {
            return Collections.emptyList();
        }

        List<String> serviceIds = portalDashboardService.getServicesIds(contextId);
        return notificationService.getAppNotificationCounts(serviceIds);
    }


    private static class NotificationData {
        int notificationsCount;
        String notificationsMessage = "";

        public NotificationData(int notificationsCount) {
            this.notificationsCount = notificationsCount;
        }

        public String getNotificationsMessage() {
            return notificationsMessage;
        }

        public NotificationData setNotificationsMessage(String notificationsMessage) {
            this.notificationsMessage = notificationsMessage;
            return this;
        }

        @JsonProperty("notificationsCount")
        public int getNotificationsCount() {
            return notificationsCount;
        }
    }
}
