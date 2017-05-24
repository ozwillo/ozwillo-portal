package org.oasis_eu.portal.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum OasisLocales {

    ENGLISH(Locale.ENGLISH),
    FRENCH(Locale.FRENCH),
    ITALIAN(Locale.ITALIAN),
    SPANISH(Locale.forLanguageTag("es")),
    CATALAN(Locale.forLanguageTag("ca")),
    TURKISH(Locale.forLanguageTag("tr")),
    BULGARIAN(Locale.forLanguageTag("bg"));

    private Locale locale;

    OasisLocales(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public static Locale getDefaultLocale() {
        return ENGLISH.getLocale();
    }

    public static List<Locale> locales() {
        return Arrays.stream(values()).map(OasisLocales::getLocale).collect(Collectors.toList());
    }
}
