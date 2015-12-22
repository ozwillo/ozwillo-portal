package org.oasis_eu.portal.services;

import java.util.Locale;

import org.oasis_eu.portal.core.constants.OasisLocales;
import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.springframework.stereotype.Service;

/**
 * Locale helper
 * User: schambon
 * Date: 8/14/14
 */
@Service
public class NameDefaults {

	/**
	 * To be used by locale FormWidget(Dropdown) to display value (especially if is a Kernel-originated user's locale)
	 * @param uiLocales "fr-FR en-GB". Though actually works also with
	 * "fr-FR,en-GB", "en-US;q=1.0,en-GB;q=0.5,fr-FR;q=0.0" http://docs.oracle.com/javase/tutorial/i18n/locale/matching.html
	 * @return null if null, "" (language of locale "und") if unknown, best otherwise
	 */
	public Languages getBestLanguage(String uiLocales) {
		return Languages.getByLocale(getBestLocale(uiLocales));
	}

	private Locale getBestLocale(String uiLocales) {
		if (uiLocales == null) {
			return null;
		}
		return UserInfo.undefinedLocaleIfNull(Locale.lookup(Locale.LanguageRange.parse(uiLocales), // works with "fr-FR en-GB"
				// (actually also with "fr-FR,en-GB", "en-US;q=1.0,en-GB;q=0.5,fr-FR;q=0.0" http://docs.oracle.com/javase/tutorial/i18n/locale/matching.html
				OasisLocales.values()));
	}

}
