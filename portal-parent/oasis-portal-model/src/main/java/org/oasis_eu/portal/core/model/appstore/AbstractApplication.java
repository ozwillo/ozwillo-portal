package org.oasis_eu.portal.core.model.appstore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;
import java.util.*;

/**
 * User: schambon
 * Date: 6/16/14
 */
abstract public class AbstractApplication extends GenericEntity {

    @JsonProperty("default_name")
    private String defaultName;
    @JsonProperty("translated_names")
    private Map<String, String> translatedNames = new HashMap<>();
    @JsonProperty("default_description")
    private String defaultDescription;
    @JsonProperty("translated_descriptions")
    private Map<String, String> translatedDescriptions = new HashMap<>();
    private URL icon;
    @JsonProperty("default_locale")
    private Locale defaultLocale;
    private URL url;
    @JsonIgnore
    private Set<AppstoreCategory> categories = new HashSet<>();
    @JsonProperty("category_ids")
    private Set<String> categoryIds = new HashSet<>();


    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public Map<String, String> getTranslatedNames() {
        return translatedNames;
    }

    public void setTranslatedNames(Map<String, String> translatedNames) {
        this.translatedNames = translatedNames;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public Map<String, String> getTranslatedDescriptions() {
        return translatedDescriptions;
    }

    public void setTranslatedDescriptions(Map<String, String> translatedDescriptions) {
        this.translatedDescriptions = translatedDescriptions;
    }

    public URL getIcon() {
        return icon;
    }

    public void setIcon(URL icon) {
        this.icon = icon;
    }

    public Set<AppstoreCategory> getCategories() {
        return categories;
    }

    public void setCategories(Set<AppstoreCategory> categories) {
        this.categories = categories;
    }

    public Set<String> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<String> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getName(Locale locale) {
        if (translatedNames.containsKey(locale.getLanguage())) {
            return translatedNames.get(locale.getLanguage());
        } else {
            return defaultName;
        }
    }

    public String getDescription(Locale locale) {
        if (translatedDescriptions.containsKey(locale.getLanguage())) {
            return translatedDescriptions.get(locale.getLanguage());
        } else {
            return defaultDescription;
        }
    }
}
