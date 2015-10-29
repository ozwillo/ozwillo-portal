package org.oasis_eu.portal.front.my.notifications;

import org.oasis_eu.portal.model.notifications.UserNotificationResponse;
import org.oasis_eu.portal.services.PortalNotificationService;
import org.oasis_eu.spring.kernel.model.NotificationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
	public UserNotificationResponse getNotifications(@RequestParam(value = "status", required = false, defaultValue = "UNREAD") NotificationStatus status) {
		return notificationService.getNotifications(status);
	}

	@RequestMapping(value = "/{notificationId}", method = RequestMethod.DELETE)
	public void archive(@PathVariable String notificationId) {
		notificationService.archive(notificationId);
	}

}
