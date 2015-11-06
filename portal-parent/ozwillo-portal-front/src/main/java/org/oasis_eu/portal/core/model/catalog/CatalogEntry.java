package org.oasis_eu.portal.core.model.catalog;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * User: schambon
 * Date: 6/16/14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "cached_service")
public class CatalogEntry implements Serializable {
	private static final long serialVersionUID = -2141845252496796600L;

	private static final Logger logger = LoggerFactory.getLogger(CatalogEntry.class);

	@Id
	private String id;

	private CatalogEntryType type;

	@JsonProperty("name")
	@NotNull
	@NotEmpty
	private String defaultName;
	@JsonProperty("description")
	@NotNull
	@NotEmpty
	private String defaultDescription;

	private Map<String, String> localizedNames = new HashMap<>();
	private Map<String, String> localizedDescriptions = new HashMap<>();
	private Map<String, String> localizedIcons = new HashMap<>();

	/* NOO accept empty for clearing up icon i.e. getting back the default one from Kernel */
	@JsonProperty("icon")
	@NotNull
	@NotEmpty
	private String defaultIcon;

	@JsonProperty("service_uri")
	private String url;

	@JsonProperty("notification_uri")
	private String notificationUrl;

	@JsonIgnore
	private List<AppstoreCategory> categories = new ArrayList<>();

	@JsonProperty("payment_option")
	private PaymentOption paymentOption;

	@JsonProperty("target_audience")
	private List<Audience> targetAudience = new ArrayList<>();

	@JsonProperty("category_ids")
	private List<String> categoryIds = new ArrayList<>();

	@JsonProperty("supported_locales")
	private List<String> supportedLocales; // ULocale

	@JsonProperty("geographical_areas")
	private Set<String> geographicalAreas = new HashSet<>(); // URI

	@JsonProperty("restricted_areas")
	private Set<String> restrictedAreas = new HashSet<>(); // URI

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
	private String policyUri;

	@JsonProperty("restricted")
	private Boolean restricted = false;


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
				", defaultName='" + defaultName +
				'}';
	}

	public Boolean getRestricted() {
		return restricted;
	}

	public void setRestricted(Boolean restricted) {
		this.restricted = restricted;
	}
}
