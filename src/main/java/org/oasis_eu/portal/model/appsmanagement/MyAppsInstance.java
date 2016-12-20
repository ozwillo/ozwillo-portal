package org.oasis_eu.portal.model.appsmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Instant;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;

import java.util.List;

/**
 * User: schambon
 * Date: 7/29/14
 */
public class MyAppsInstance {

    ApplicationInstance applicationInstance;

    List<MyAppsService> myAppsServices;

    private String name;

    String icon;

    /**
     * optional
     */
    @JsonProperty("deletion_planned")
    Instant deletionPlanned;

    @JsonProperty("status_change_requester_label")
    String statusChangeRequesterLabel;

    // Default constructor is needed by Jackson to instantiate object from JSON
    public MyAppsInstance() {
    }

    public MyAppsInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public List<MyAppsService> getServices() {
        return myAppsServices;
    }

    public void setServices(List<MyAppsService> myAppsServices) {
        this.myAppsServices = myAppsServices;
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
