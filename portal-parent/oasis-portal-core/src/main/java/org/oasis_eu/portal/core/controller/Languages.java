package org.oasis_eu.portal.core.controller;

import org.oasis_eu.portal.core.constants.OasisLocales;

import java.util.Locale;

/**
 * User: schambon
 * Date: 6/11/14
 */
public enum Languages {

    ENGLISH("English", OasisLocales.ENGLISH),
    FRENCH("Français", OasisLocales.FRENCH),
    ITALIAN("Italiano", OasisLocales.ITALIAN),
    SPANISH("Español", OasisLocales.SPANISH),
    CATALAN("Català", OasisLocales.CATALAN),
    BULGARIAN("български", OasisLocales.BULGARIAN),
    TURKISH("Türkçe", OasisLocales.TURKISH);

    private String name;
    private Locale locale;

    private Languages(String name, Locale locale) {
        this.name = name;
        this.locale = locale;
    }

    public String getName() {
        return name;

    }
    public String getLanguage() {
        return locale.getLanguage();
    }

    public Locale getLocale() {
        return locale;
    }
    
    public static Languages getByLocale(Locale locale) {
		return getByLocale(locale, null);
    }
    
    public static Languages getByLocale(Locale locale, Languages defaultLang) {
        for (Languages l : values()) {
            if (l.getLanguage().equals(locale.getLanguage())) {
                return l;
            }
        }
		return defaultLang;
    }

    public static Languages getByLanguageTag(String languageTag) {
        for (Languages l : values()) {
            if (l.getLanguage().equals(languageTag)) {
                return l;
            }
        }
        return null;
    }
}
