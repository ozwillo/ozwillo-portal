package org.oasis_eu.portal.front.my.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.portal.services.PortalNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * User: schambon
 * Date: 8/14/14
 */
@RestController
@RequestMapping("/my/api")
public class NotificationsAJAXServices extends BaseAJAXServices{

    @Autowired
    private PortalNotificationService notificationService;

    @Autowired
    private MessageSource messageSource;

    @Value("${application.notificationsEnabled:true}")
    private boolean notificationsEnabled;


    @RequestMapping(method = RequestMethod.GET, value="/notifications")
    @ResponseBody
    public NotificationData getNotificationData(HttpServletRequest request) {
        int count = notificationService.countNotifications();
        return new NotificationData(count).setNotificationsMessage(messageSource.getMessage("my.n_notifications", new Object[]{Integer.valueOf(count)}, RequestContextUtils.getLocale(request)));
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
