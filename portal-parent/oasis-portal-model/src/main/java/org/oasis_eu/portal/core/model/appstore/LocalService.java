package org.oasis_eu.portal.core.model.appstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * User: schambon
 * Date: 5/14/14
 */
public class LocalService extends GenericEntity {
    private static final Logger logger = LoggerFactory.getLogger(LocalService.class);

    private String id;
    private Map<String, String> names = new HashMap<>();
    private Map<String, String> descriptions = new HashMap<>();

    private URL url;

    private String applicationId;

    /**
     * Name that is used in case of a missing translation
     */
    private String defaultName;
    private String defaultDescription;


    private String territoryId;

    public String getName(Locale locale) {
        String name = names.get(locale.getLanguage());
        if (name != null) {
            return name;
        } else {
            logger.warn("Cannot find translated name for local service \"{}\" (id: {}) in language {}", defaultName, id, locale);
            return defaultName;
        }
    }

    public void setName(Locale locale, String name) {
        names.put(locale.getLanguage(), name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getTerritoryId() {
        return territoryId;
    }

    public void setTerritoryId(String territoryId) {
        this.territoryId = territoryId;
    }

    public void setDescription(Locale locale, String desc) {
        descriptions.put(locale.getLanguage(), desc);
    }

    public String getDescription(Locale locale) {
        String desc = descriptions.get(locale.getLanguage());
        if (desc == null) {
            logger.warn("Cannot find translated description for local service \"{}\" (id: {}) in language {}", defaultName, id, locale);
            return defaultDescription;
        } else {
            return desc;
        }
    }

    public Map<String, String > getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Map<String, String> descriptions) {
        this.descriptions = descriptions;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

}
