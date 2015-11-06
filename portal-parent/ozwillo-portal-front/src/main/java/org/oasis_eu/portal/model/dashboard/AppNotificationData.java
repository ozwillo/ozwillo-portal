package org.oasis_eu.portal.model.dashboard;

/**
* User: schambon
* Date: 6/18/14
*/
public class AppNotificationData {
	String applicationId;
	int count;

	public AppNotificationData(String applicationId, int count) {
		this.applicationId = applicationId;
		this.count = count;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public int getCount() {
		return count;
	}
}
