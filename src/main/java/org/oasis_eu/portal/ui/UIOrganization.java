package org.oasis_eu.portal.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.config.CustomInstantSerializer;
import org.oasis_eu.portal.model.app.instance.MyAppsInstance;
import org.oasis_eu.portal.model.app.service.InstanceService;
import org.oasis_eu.spring.kernel.model.OrganizationStatus;
import org.oasis_eu.spring.kernel.model.OrganizationType;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * User: schambon
 * Date: 10/24/14
 */
public class UIOrganization {
    @JsonProperty
    @NotNull
    @NotEmpty
    private String id;

    @JsonProperty
    @NotNull
    @NotEmpty
    private String name;

    @JsonProperty
    @NotNull
    private OrganizationType type;

    @JsonProperty("territory_id")
    private URI territoryId;

    @JsonProperty("territory_label")
    private String territoryLabel;

    @JsonProperty("dc_id")
    private URI dcId;

    /**
     * optional
     */
    private OrganizationStatus status;

    /**
     * optional
     */
    @JsonProperty("status_changed")
    @JsonSerialize(using = CustomInstantSerializer.class)
    private Instant statusChanged;

    /**
     * optional
     */
    @JsonProperty("deletion_planned")
    @JsonSerialize(using = CustomInstantSerializer.class)
    private Instant deletionPlanned;

    /**
     * optional
     */
    @JsonProperty("status_change_requester_id")
    private String statusChangeRequesterId;

    /**
     * optional
     */
    @JsonProperty("status_change_requester_label")
    private String statusChangeRequesterLabel;


    @JsonProperty
    private List<MyAppsInstance> instances;

    @JsonProperty
    private List<InstanceService> services;

    @JsonProperty
    private List<UIOrganizationMember> members;

    @JsonProperty
    boolean admin;



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

    public OrganizationType getType() {
        return type;
    }

    public void setType(OrganizationType type) {
        this.type = type;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public URI getTerritoryId() {
        return territoryId;
    }

    public void setTerritoryId(URI territoryId) {
        this.territoryId = territoryId;
    }

    public String getTerritoryLabel() {
        return territoryLabel;
    }

    public void setTerritoryLabel(String territoryLabel) {
        this.territoryLabel = territoryLabel;
    }

    public URI getDcId() {
        return dcId;
    }

    public void setDcId(URI dcId) {
        this.dcId = dcId;
    }

    public OrganizationStatus getStatus() {
        return status;
    }

    public void setStatus(OrganizationStatus status) {
        this.status = status;
    }

    public Instant getStatusChanged() {
        return statusChanged;
    }

    public void setStatusChanged(Instant statusChanged) {
        this.statusChanged = statusChanged;
    }

    public Instant getDeletionPlanned() {
        return deletionPlanned;
    }

    public void setDeletionPlanned(Instant deletionPlanned) {
        this.deletionPlanned = deletionPlanned;
    }

    public String getStatusChangeRequesterId() {
        return statusChangeRequesterId;
    }

    public void setStatusChangeRequesterId(String statusChangeRequesterId) {
        this.statusChangeRequesterId = statusChangeRequesterId;
    }

    public String getStatusChangeRequesterLabel() {
        return statusChangeRequesterLabel;
    }

    public void setStatusChangeRequesterLabel(String statusChangeRequesterLabel) {
        this.statusChangeRequesterLabel = statusChangeRequesterLabel;
    }

    public List<InstanceService> getServices() {
        return services;
    }

    public void setServices(List<InstanceService> services) {
        this.services = services;
    }

    public List<UIOrganizationMember> getMembers() {
        return members;
    }

    public void setMembers(List<UIOrganizationMember> members) {
        this.members = members;
    }

    public List<MyAppsInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<MyAppsInstance> instances) {
        this.instances = instances;
    }

    @Override
    public String toString() {
        return "{" +
            "id:'" + id + '\'' +
            ", name:'" + name + '\'' +
            ", type:" + type +
            ", territory_id:" + territoryId +
            ", dcId:" + dcId +
            ", status:" + status +
            ", admin:" + admin +
            '}';
    }
}
