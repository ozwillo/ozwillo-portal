package org.oasis_eu.portal.model.sitemap;

public class StyleProperty {
    String key = "";

    String value = "";

    public StyleProperty() {
    }

    public StyleProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
