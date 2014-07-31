package org.oasis_eu.portal.core.mongo.model.temp;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

/**
 * User: schambon
 * Date: 7/29/14
 */
public class ApplicationInstanceRegistration {

    @Id
    private String _id;

    @JsonProperty("instance_id")
    private String instanceId;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("organization_id")
    private String organizationId;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
