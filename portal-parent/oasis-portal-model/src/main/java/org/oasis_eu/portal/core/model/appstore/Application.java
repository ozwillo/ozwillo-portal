package org.oasis_eu.portal.core.model.appstore;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.URL;
import java.util.*;

/**
 * User: schambon
 * Date: 5/14/14
 */
public class Application extends GenericEntity {

    private String defaultName;
    private Map<String, String> translatedNames = new HashMap<>();
    private String defaultDescription;
    private Map<String, String> translatedDescriptions = new HashMap<>();
    private PaymentOption paymentOption;
    private Map<Audience, Boolean> targetAudience = new HashMap<>();
    private URL icon;
    private Locale defaultLocale;
    private URL url;

    @JsonIgnore
    private Set<AppstoreCategory> categories = new HashSet<>();

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

    public PaymentOption getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(PaymentOption paymentOption) {
        this.paymentOption = paymentOption;
    }

    public Map<Audience, Boolean> getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(Map<Audience, Boolean> targetAudience) {
        this.targetAudience = targetAudience;
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

    public boolean isTargetedTo(Audience audience) {
        return targetAudience.get(audience);
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
