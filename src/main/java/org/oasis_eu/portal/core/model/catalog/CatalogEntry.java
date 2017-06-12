package org.oasis_eu.portal.core.model.catalog;

import com.fasterxml.jackson.annotation.*;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * User: schambon
 * Date: 6/16/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogEntry implements Serializable {
    private static final long serialVersionUID = -2141845252496796600L;

    private static final Logger logger = LoggerFactory.getLogger(CatalogEntry.class);

    private CatalogEntryType type;

    private String id;

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @NotEmpty
    private String description;

    /* NOO accept empty for clearing up icon i.e. getting back the default one from Kernel */
    @NotNull
    @NotEmpty
    private String icon;

    @JsonProperty("provider_id")
    private String providerId;

    @JsonProperty("payment_option")
    private PaymentOption paymentOption;

    @JsonProperty("target_audience")
    private List<Audience> targetAudience = new ArrayList<>();

    @JsonProperty("category_ids")
    private List<String> categoryIds = new ArrayList<>();

    @JsonIgnore
    private List<AppstoreCategory> categories = new ArrayList<>();

    @JsonProperty("supported_locales")
    private List<String> supportedLocales; // ULocale

    @JsonProperty("geographical_areas")
    private Set<String> geographicalAreas = new HashSet<>(); // URI

    @JsonProperty("restricted_areas")
    private Set<String> restrictedAreas = new HashSet<>(); // URI

    private Map<String, String> localizedNames = new HashMap<>();
    private Map<String, String> localizedDescriptions = new HashMap<>();
    private Map<String, String> localizedIcons = new HashMap<>();

    private List<String> contacts;

    @JsonProperty("screenshot_uris")
    private List<String> screenshotUris;

    @JsonProperty("tos_uri")
    private String tosUri;

    @JsonProperty("policy_uri")
    private String policyUri;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CatalogEntryType getType() {
        return type;
    }

    public void setType(CatalogEntryType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public void setLocalizedNames(Map<String, String> localizedNames) {
        this.localizedNames = localizedNames;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @JsonIgnore
    public void setLocalizedDescriptions(Map<String, String> localizedDescriptions) {
        this.localizedDescriptions = localizedDescriptions;
    }

    @JsonIgnore
    public void setLocalizedIcons(Map<String, String> localizedIcons) {
        this.localizedIcons = localizedIcons;
    }


    @JsonAnySetter
    public void setTranslation(String key, String value) {

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

    @JsonAnyGetter
    public Map<String, String> getTranslations() {

        Map<String, String> result = new HashMap<>();
        localizedNames.entrySet().forEach(e -> result.put("name#" + e.getKey(), e.getValue()));
        localizedDescriptions.entrySet().forEach(e -> result.put("description#" + e.getKey(), e.getValue()));
        localizedIcons.entrySet().forEach(e -> result.put("icon#" + e.getKey(), e.getValue()));

        return result;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<AppstoreCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<AppstoreCategory> categories) {
        this.categories = categories;
    }

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<String> getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(List<String> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public Set<String> getGeographicalAreas() {
        return geographicalAreas;
    }

    public void setGeographicalAreas(Set<String> geographicalAreas) {
        this.geographicalAreas = geographicalAreas;
    }

    public Set<String> getRestrictedAreas() {
        return restrictedAreas;
    }

    public void setRestrictedAreas(Set<String> restrictedAreas) {
        this.restrictedAreas = restrictedAreas;
    }

    public PaymentOption getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(PaymentOption paymentOption) {
        this.paymentOption = paymentOption;
    }

    public List<Audience> getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(List<Audience> targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getName(Locale locale) {
        if (localizedNames.containsKey(locale.getLanguage())) {
            return localizedNames.get(locale.getLanguage());
        } else {
            return name;
        }
    }

    public String getDescription(Locale locale) {
        if (localizedDescriptions.containsKey(locale.getLanguage())) {
            return localizedDescriptions.get(locale.getLanguage());
        } else {
            return description != null ? description : "";
        }
    }

    public String getIcon(Locale locale) {
        if (localizedIcons.containsKey(locale.getLanguage())) {
            return localizedIcons.get(locale.getLanguage());
        } else {
            return icon;
        }
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public List<String> getScreenshotUris() {
        return screenshotUris;
    }

    public void setScreenshotUris(List<String> screenshotUris) {
        this.screenshotUris = screenshotUris;
    }

    public String getPolicyUri() {
        return policyUri;
    }

    public void setPolicyUri(String policyUri) {
        this.policyUri = policyUri;
    }

    public String getTosUri() {
        return tosUri;
    }

    public void setTosUri(String tosUri) {
        this.tosUri = tosUri;
    }

    @Override
    public String toString() {
        return "CatalogEntry{" +
            "id='" + id + '\'' +
            ", type=" + type +
            ", name='" + name +
            '}';
    }

}
