package org.oasis_eu.portal.core.model.catalog;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.spring.kernel.model.instance.ScopeNeeded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: schambon
 * Date: 7/29/14
 */
public class ApplicationInstance {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInstance.class);

    @JsonProperty("application_id")
    String applicationId;
    @JsonProperty("instance_id")
    String instanceId;
    InstantiationStatus status;
    String name;
    String description;
    String icon;

    @JsonProperty("needed_scopes")
    List<ScopeNeeded> neededScopes;

    @JsonIgnore
    Map<String, String> localizedNames = new HashMap<>();
    @JsonIgnore
    Map<String, String> localizedDescriptions = new HashMap<>();
    @JsonIgnore
    Map<String, String> localidedIcons = new HashMap<>();

    @JsonProperty("provider_id")
    String providerId;


    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public InstantiationStatus getStatus() {
        return status;
    }

    public void setStatus(InstantiationStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public List<ScopeNeeded> getNeededScopes() {
        return neededScopes;
    }

    public void setNeededScopes(List<ScopeNeeded> neededScopes) {
        this.neededScopes = neededScopes;
    }

    @JsonAnySetter
    public void anySetter(String key, String value) {
        if (key.startsWith("name#")) {
            localizedNames.put(key.substring("name#".length()), value);
        } else if (key.startsWith("description#")) {
            localizedDescriptions.put(key.substring("description#".length()), value);
        } else if (key.startsWith("icon#")) {
            localidedIcons.put(key.substring("icon#".length()), value);
        } else {
            logger.info("Discarding unknown property {}", key);
        }
    }

    public static enum InstantiationStatus {
        PENDING, RUNNING;
    }

    @Override
    public String toString() {
        return "ApplicationInstance{" +
                "applicationId='" + applicationId + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                ", neededScopes=" + neededScopes +
                ", localizedNames=" + localizedNames +
                ", localizedDescriptions=" + localizedDescriptions +
                ", localidedIcons=" + localidedIcons +
                ", providerId='" + providerId + '\'' +
                '}';
    }
}
