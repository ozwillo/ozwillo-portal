package org.oasis_eu.portal.model.notifications;

import java.util.Collections;
import java.util.List;

/**
 * User: schambon
 * Date: 4/15/15
 */
public class UserNotificationResponse {

	private List<UserNotification> notifications = Collections.emptyList();
	private List<NotifApp> apps = Collections.emptyList();

	public UserNotificationResponse(List<UserNotification> notifications, List<NotifApp> apps) {
		this.notifications = notifications;
		this.apps = apps;
	}

	public UserNotificationResponse() {
	}

	public List<UserNotification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<UserNotification> notifications) {
		this.notifications = notifications;
	}

	public List<NotifApp> getApps() {
		return apps;
	}

	public void setApps(List<NotifApp> apps) {
		this.apps = apps;
	}
}
