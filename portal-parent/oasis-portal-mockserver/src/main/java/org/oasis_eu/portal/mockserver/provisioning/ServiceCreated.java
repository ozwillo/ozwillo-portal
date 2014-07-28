package org.oasis_eu.portal.mockserver.provisioning;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.core.model.appstore.Audience;
import org.oasis_eu.portal.core.model.appstore.PaymentOption;

import java.util.*;

/**
 * User: schambon
 * Date: 7/1/14
 */
@Deprecated
public class ServiceCreated {

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    private String identifier;


    @JsonProperty("name")
    private String defaultName;
    @JsonProperty("description")
    private String defaultDescription;

    private Map<String, String> localizedNames = new HashMap<>();

    private Map<String, String> localizedDescriptions = new HashMap<>();

    private Map<String, String> localizedIcons = new HashMap<>();

    @JsonProperty("icon")
    private String defaultIcon;

    @JsonProperty("default_locale")
    private Locale defaultLocale;

    private String url;

    @JsonProperty("notification_url")
    private String notificationUrl;

    @JsonProperty("redirect_url")
    private String redirectUrl;

    @JsonProperty("category_ids")
    private List<String> categoryIds = new ArrayList<>();

    @JsonProperty("payment_option")
    private PaymentOption paymentOption;

    @JsonProperty("target_audience")
    private List<Audience> targetAudience;

    @JsonProperty("territory_id")
    private String territoryId;

    @JsonProperty("provider_organization_id")
    private String providerId;

    private boolean visible = false;


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

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
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

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
