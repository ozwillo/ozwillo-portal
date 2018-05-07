package org.oasis_eu.portal.core.model.catalog;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.spring.kernel.model.instance.ScopeNeeded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * User: schambon
 * Date: 7/29/14
 */
public class ApplicationInstance {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInstance.class);

    @JsonProperty("application_id")
    String applicationId;

    @JsonProperty("id")
    String instanceId;

    InstantiationStatus status;

    /**
     * optional
     */
    @JsonProperty("status_changed")
    Instant statusChanged;

    /**
     * optional
     */
    @JsonProperty("status_change_requester_id")
    String statusChangeRequesterId;

    /**
     * optional ; if Personal app must be the user ; ID of the user who created the instance.
     */
    @JsonProperty("instantiator_id")
    String instantiatorId;

    @JsonProperty("needed_scopes")
    List<ScopeNeeded> neededScopes;

    // Kernel Common Properties
    @JsonProperty("name")
    @NotNull
    @NotEmpty
    String defaultName;

    @JsonProperty("description")
    @NotNull
    @NotEmpty
    String defaultDescription;

    @JsonProperty("icon")
    @NotNull
    @NotEmpty
    String defaultIcon;

    private Map<String, String> localizedNames = new HashMap<>();
    private Map<String, String> localizedDescriptions = new HashMap<>();
    private Map<String, String> localizedIcons = new HashMap<>();

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

    public Instant getStatusChanged() {
        return statusChanged;
    }

    public void setStatusChanged(Instant statusChanged) {
        this.statusChanged = statusChanged;
    }

    public String getStatusChangeRequesterId() {
        return statusChangeRequesterId;
    }

    public void setStatusChangeRequesterId(String statusChangeRequesterId) {
        this.statusChangeRequesterId = statusChangeRequesterId;
    }

    public String getName(Locale locale) {
        if (localizedNames.containsKey(locale.getLanguage())) {
            return localizedNames.get(locale.getLanguage());
        } else {
            return defaultName;
        }
    }

    public String getDescription(Locale locale) {
        if (localizedDescriptions.containsKey(locale.getLanguage())) {
            return localizedDescriptions.get(locale.getLanguage());
        } else {
            return defaultDescription != null ? defaultDescription : "";
        }
    }

    public String getIcon(Locale locale) {
        if (localizedIcons.containsKey(locale.getLanguage())) {
            return localizedIcons.get(locale.getLanguage());
        } else {
            return defaultIcon;
        }
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public String getDefaultIcon() {
        return defaultIcon;
    }

    public void setDefaultIcon(String defaultIcon) {
        this.defaultIcon = defaultIcon;
    }

    @JsonIgnore
    public void setLocalizedNames(Map<String, String> localizedNames) {
        this.localizedNames = localizedNames;
    }

    @JsonIgnore
    public void setLocalizedDescriptions(Map<String, String> localizedDescriptions) {
        this.localizedDescriptions = localizedDescriptions;
    }

    @JsonIgnore
    public void setLocalizedIcons(Map<String, String> localizedIcons) {
        this.localizedIcons = localizedIcons;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getInstantiatorId() {
        return instantiatorId;
    }

    public void setInstantiatorId(String instantiatorId) {
        this.instantiatorId = instantiatorId;
    }

    public List<ScopeNeeded> getNeededScopes() {
        return neededScopes;
    }

    public void setNeededScopes(List<ScopeNeeded> neededScopes) {
        this.neededScopes = neededScopes;
    }

    @JsonAnyGetter
    public Map<String, String> anyGetter() {
        Map<String, String> result = new HashMap<>();

        localizedNames.entrySet().forEach(e -> result.put("name#" + e.getKey(), e.getValue()));
        localizedDescriptions.entrySet().forEach(e -> result.put("description#" + e.getKey(), e.getValue()));
        localizedIcons.entrySet().forEach(e -> result.put("icon#" + e.getKey(), e.getValue()));

        return result;
    }

    @JsonAnySetter
    public void anySetter(String key, String value) {
        if (key.startsWith("name#")) {
            localizedNames.put(key.substring("name#".length()), value);
        } else if (key.startsWith("description#")) {
            localizedDescriptions.put(key.substring("description#".length()), value);
        } else if (key.startsWith("icon#")) {
            localizedIcons.put(key.substring("icon#".length()), value);
        } else {
            logger.debug("Discarding unknown property {}", key);
        }
    }

    public enum InstantiationStatus {
        PENDING, RUNNING, STOPPED // STOPPED means trashed (replaces deletion)
    }

    @Override
    public String toString() {
        return "ApplicationInstance{" +
            "applicationId='" + applicationId + '\'' +
            ", instanceId='" + instanceId + '\'' +
            ", status=" + status +
            ", statusChanged=" + statusChanged +
            ", statusChangeRequesterId=" + statusChangeRequesterId +
            ", defaultName='" + defaultName + '\'' +
            ", defaultDescription='" + defaultDescription + '\'' +
            ", defaultIcon='" + defaultIcon + '\'' +
            ", neededScopes=" + neededScopes +
            ", localizedNames=" + localizedNames +
            ", localizedDescriptions=" + localizedDescriptions +
            ", localidedIcons=" + localizedIcons +
            ", providerId='" + providerId + '\'' +
            '}';
    }
}
