package org.oasis_eu.portal.model;

import org.joda.time.Instant;

/**
 * User: schambon
 * Date: 6/13/14
 */
public class UserNotification {
    String appName;
    String formattedText;
    Instant date;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getFormattedText() {
        return formattedText;
    }

    public void setFormattedText(String formattedText) {
        this.formattedText = formattedText;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }
}
