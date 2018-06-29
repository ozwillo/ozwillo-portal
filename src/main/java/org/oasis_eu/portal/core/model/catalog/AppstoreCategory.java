package org.oasis_eu.portal.core.model.catalog;

import org.oasis_eu.portal.core.model.appstore.GenericEntity;
import org.oasis_eu.portal.model.OasisLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * User: schambon
 * Date: 5/28/14
 */
public class AppstoreCategory extends GenericEntity {

    private static final Logger logger = LoggerFactory.getLogger(AppstoreCategory.class);

    Map<String, String> names = new HashMap<>();

    public AppstoreCategory() {

    }

    public AppstoreCategory(String id, Map<String, String> names) {
        this.id = id;
        this.names = names;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public String getLocalizedName(Locale locale) {
        String name = names.get(locale.getLanguage());
        if (name == null) {
            logger.warn("Cannot findApplication translation for category {} in language {}", id, locale.getLanguage());
            name = names.get(OasisLocales.getDefaultLocale().getLanguage());
            if (name == null) {
                logger.error("Cannot findApplication translation for category {} in default language ({})", id, OasisLocales.getDefaultLocale().getLanguage());
            }
        }

        // if it is still null then there is nothing we can do!
        return name;
    }

    public void setLocalizedName(Locale locale, String name) {
        names.put(locale.getLanguage(), name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppstoreCategory that = (AppstoreCategory) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

