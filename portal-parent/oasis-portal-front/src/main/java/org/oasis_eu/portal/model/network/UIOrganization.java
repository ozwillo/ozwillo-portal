package org.oasis_eu.portal.model.network;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.Instant;
import org.oasis_eu.spring.kernel.model.OrganizationStatus;
import org.oasis_eu.spring.kernel.model.OrganizationType;

import javax.validation.constraints.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * User: schambon
 * Date: 10/24/14
 */
public class UIOrganization {
    @JsonProperty
    @NotNull
    @NotEmpty
    String id;
    @JsonProperty
    @NotNull
    @NotEmpty
    String name;
    @JsonProperty
    @NotNull
    OrganizationType type;
    @JsonProperty("territory_id")
    URI territoryId;
    @JsonProperty("territory_label")
    String territoryLabel;
    @JsonProperty("dc_id")
    URI dcId;

    /** optional */
    OrganizationStatus status;
    /** optional */
    @JsonProperty("status_changed")
    Instant statusChanged;
    /** optional */
    @JsonProperty("deletion_planned")
    Instant deletionPlanned;
    /** optional */
    @JsonProperty("status_change_requester_id")
    String statusChangeRequesterId;
    /** optional */
    @JsonProperty("status_change_requester_label")
    String statusChangeRequesterLabel;

    @JsonProperty
    boolean admin;

    @JsonProperty
    List<UIOrganizationMember> members = new ArrayList<>();

    @JsonProperty
    List<UIPendingOrganizationMember> pendingMemberships = new ArrayList<>();

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

    public List<UIOrganizationMember> getMembers() {
        return members;
    }

    public void setMembers(List<UIOrganizationMember> members) {
        this.members = members;
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

    public List<UIPendingOrganizationMember> getPendingMemberships() {
        return pendingMemberships;
    }

    public void setPendingMemberships(List<UIPendingOrganizationMember> pendingMemberships) {
        this.pendingMemberships = pendingMemberships;
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
                ", members:" + members +
                ", pending_memberships:" + pendingMemberships +
                '}';
    }
}
