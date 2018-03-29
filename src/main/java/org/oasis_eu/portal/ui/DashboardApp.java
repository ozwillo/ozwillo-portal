package org.oasis_eu.portal.ui;

/**
 * User: schambon
 * Date: 11/19/14
 */
public class DashboardApp {

    private String id;
    private String name;
    private String url;
    private String icon;
    private String serviceId;
    private String notificationUrl;
    private int notificationCount;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }

    @Override
    public String toString() {
        return "DashboardApp{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", serviceId='" + serviceId + '\'' +
            '}';
    }
}
