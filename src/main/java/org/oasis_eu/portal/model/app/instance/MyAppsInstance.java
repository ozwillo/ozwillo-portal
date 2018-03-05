package org.oasis_eu.portal.model.app.instance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.oasis_eu.portal.config.CustomInstantSerializer;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.model.app.service.InstanceService;

import java.time.Instant;
import java.util.List;

/**
 * User: schambon
 * Date: 7/29/14
 */
public class MyAppsInstance {

    private ApplicationInstance applicationInstance;

    private List<InstanceService> services;

    private String name;

    private String icon;

    /**
     * optional
     */
    @JsonProperty("deletion_planned")
    @JsonSerialize(using = CustomInstantSerializer.class)
    private Instant deletionPlanned;

    @JsonProperty("status_change_requester_label")
    private String statusChangeRequesterLabel;

    // Default constructor is needed by Jackson to instantiate object from JSON
    public MyAppsInstance() { }

    public MyAppsInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public List<InstanceService> getServices() {
        return services;
    }

    public void setServices(List<InstanceService> services) {
        this.services = services;
    }

    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }

    public void setApplicationInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name != null ? this.name : this.applicationInstance.getDefaultName();
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public String getId() {
        return applicationInstance.getInstanceId();
    }

    public Instant getDeletionPlanned() {
        return deletionPlanned;
    }

    public void setDeletionPlanned(Instant deletionPlanned) {
        this.deletionPlanned = deletionPlanned;
    }

    public String getStatusChangeRequesterLabel() {
        return statusChangeRequesterLabel;
    }

    public void setStatusChangeRequesterLabel(String statusChangeRequesterLabel) {
        this.statusChangeRequesterLabel = statusChangeRequesterLabel;
    }
}
