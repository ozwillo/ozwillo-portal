package org.oasis_eu.portal.model.kernel.store;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceEntry extends CatalogEntry {

    private String visibility;

    @JsonProperty("access_control")
    private String accessControl;

    @JsonProperty("service_uri")
    private String url;

    @JsonProperty("notification_uri")
    private String notificationUrl;

    @JsonProperty("local_id")
    private String localId;

    @JsonProperty("instance_id")
    private String instanceId;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("post_logout_redirect_uris")
    private List<String> postLogoutRedirectUris;

    private Set<String> portals;

    private Status status;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(String accessControl) {
        this.accessControl = accessControl;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public void setPostLogoutRedirectUris(List<String> postLogoutRedirectUris) {
        this.postLogoutRedirectUris = postLogoutRedirectUris;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        AVAILABLE, NOT_AVAILABLE;
    }

    public Set<String> getPortals() {
        return portals;
    }

    public void setPortals(Set<String> portals) {
        this.portals = portals;
    }
}
