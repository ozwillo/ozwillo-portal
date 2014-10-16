package org.oasis_eu.portal.core.model.catalog;

import com.fasterxml.jackson.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * User: schambon
 * Date: 6/16/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogEntry  {

    private static final Logger logger = LoggerFactory.getLogger(CatalogEntry.class);

    private String id;

    private CatalogEntryType type;

    @JsonProperty("name")
    private String defaultName;
    @JsonProperty("description")
    private String defaultDescription;

    private Map<String, String> localizedNames = new HashMap<>();

    private Map<String, String> localizedDescriptions = new HashMap<String, String>();

    private Map<String, String> localizedIcons = new HashMap<>();

    @JsonProperty("icon")
    private String defaultIcon;

    @JsonProperty("service_uri")
    private String url;

    @JsonProperty("notification_uri")
    private String notificationUrl;

    @JsonIgnore
    private List<AppstoreCategory> categories = new ArrayList<>();

    @JsonProperty("category_ids")
    private List<String> categoryIds = new ArrayList<>();

    @JsonProperty("payment_option")
    private PaymentOption paymentOption;

    @JsonProperty("target_audience")
    private List<Audience> targetAudience = new ArrayList<>();

    @JsonProperty("territory_id")
    private String territoryId;

    @JsonProperty("provider_id")
    private String providerId;

    @JsonProperty("instance_id")
    private String instanceId;

    /**
     * App store publication status
     */
    @JsonProperty("visible")
    private boolean visible = false;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris;

    @JsonProperty("post_logout_redirect_uris")
    private List<String> postLogoutRedirectUris;

    @JsonProperty("contacts")
    private List<String> contacts;

    @JsonProperty("screenshot_uris")
    private List<String> screenshotUris;

    @JsonProperty("tos_uri")
    private String tosUri;

    @JsonProperty("policy_uri")
    private String privacyUri;





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

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    @JsonIgnore
    public void setLocalizedNames(Map<String, String> localizedNames) {
        this.localizedNames = localizedNames;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
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

    public String getDefaultIcon() {
        return defaultIcon;
    }

    public void setDefaultIcon(String defaultIcon) {
        this.defaultIcon = defaultIcon;
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

    public String getTerritoryId() {
        return territoryId;
    }

    public void setTerritoryId(String territoryId) {
        this.territoryId = territoryId;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
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
            return defaultName;
        }
    }

    public String getDescription(Locale locale) {
        if (localizedDescriptions.containsKey(locale.getLanguage())) {
            return localizedDescriptions.get(locale.getLanguage());
        } else {
            return defaultDescription;
        }
    }

    public String getIcon(Locale locale) {
        if (localizedIcons.containsKey(locale.getLanguage())) {
            return localizedIcons.get(locale.getLanguage());
        } else {
            return defaultIcon;
        }
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public void setPostLogoutRedirectUris(List<String> postLogoutRedirectUris) {
        this.postLogoutRedirectUris = postLogoutRedirectUris;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
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

    public String getPrivacyUri() {
        return privacyUri;
    }

    public void setPrivacyUri(String privacyUri) {
        this.privacyUri = privacyUri;
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
                ", defaultName='" + defaultName +
                '}';
    }
}
