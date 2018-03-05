package org.oasis_eu.portal.model.notifications;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.oasis_eu.portal.config.CustomInstantSerializer;
import org.oasis_eu.spring.kernel.model.NotificationStatus;

/**
 * User: schambon
 * Date: 6/13/14
 */
public class UserNotification {
    private String appName;
    private String formattedText;
    @JsonSerialize(using = CustomInstantSerializer.class)
    private Instant date;
    private String dateText;
    private String id;
    private String url;
    private String actionText;
    private String serviceId;
    private String applicationId;
    private NotificationStatus status;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDateText() {
        return dateText;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

}
