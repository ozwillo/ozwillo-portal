package org.oasis_eu.portal.services;

import org.oasis_eu.portal.model.OasisLocales;
import org.oasis_eu.portal.model.Languages;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class LocaleService {

    /**
     * To be used by locale FormWidget(Dropdown) to display value (especially if is a Kernel-originated user's locale)
     *
     * @param uiLocales "fr-FR en-GB". Though actually works also with
     *                  "fr-FR,en-GB", "en-US;q=1.0,en-GB;q=0.5,fr-FR;q=0.0" http://docs.oracle.com/javase/tutorial/i18n/locale/matching.html
     * @return null if null, "" (language of locale "und") if unknown, best otherwise
     */
    public Languages getBestLanguage(String uiLocales) {
        return Languages.getByLocale(getBestLocale(uiLocales));
    }

    private Locale getBestLocale(String uiLocales) {
        if (uiLocales == null) {
            return null;
        }
        return undefinedLocaleIfNull(Locale.lookup(Locale.LanguageRange.parse(uiLocales), // works with "fr-FR en-GB"
            // (actually also with "fr-FR,en-GB", "en-US;q=1.0,en-GB;q=0.5,fr-FR;q=0.0" http://docs.oracle.com/javase/tutorial/i18n/locale/matching.html
            OasisLocales.locales()));
    }

    /**
     * Negociates the best locale for the user among the given available ones
     * @return null if null, "und" Locale if none, best otherwise
     */
    public Locale getBestLocale(Locale locale, List<Locale> localeList) {
        if (locale == null) {
            return null;
        }
        return undefinedLocaleIfNull(Locale.lookup(getLocaleAsLanguageRanges(locale), localeList));
    }

    private static Locale undefinedLocaleIfNull(Locale locale) {
        return (locale != null) ? locale : Locale.forLanguageTag("und");
    }

    /**
     * Allows to do what getBestLocale/LanguageTag does using Locale.lookup(Tag)()
     * @return locale parsed as LanguageRanges, locale being ex. "fr-FR en-GB es"
     * (actually "fr-FR,en-GB", "en-US;q=1.0,en-GB;q=0.5,fr-FR;q=0.0" also works)
     */
    private List<Locale.LanguageRange> getLocaleAsLanguageRanges(Locale locale) {
        if (locale == null) {
            return null;
        }
        return Locale.LanguageRange.parse(locale.getLanguage()); // works with "fr-FR en-GB"
        // (actually also with "fr-FR,en-GB", "en-US;q=1.0,en-GB;q=0.5,fr-FR;q=0.0" http://docs.oracle.com/javase/tutorial/i18n/locale/matching.html
    }
}
