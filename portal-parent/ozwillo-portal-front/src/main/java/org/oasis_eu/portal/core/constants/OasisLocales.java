package org.oasis_eu.portal.core.constants;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * TODO refactor as an enum
 * User: schambon
 * Date: 5/28/14
 */
public class OasisLocales {

	public static final Locale ENGLISH = Locale.ENGLISH;

	public static final Locale FRENCH = Locale.FRENCH;
	public static final Locale ITALIAN = Locale.ITALIAN;
	public static final Locale SPANISH = Locale.forLanguageTag("es");
	public static final Locale CATALAN = Locale.forLanguageTag("ca");
	public static final Locale TURKISH = Locale.forLanguageTag("tr");
	public static final Locale BULGARIAN = Locale.forLanguageTag("bg");


	private static List<Locale> values = Arrays.asList(ENGLISH, FRENCH, ITALIAN, SPANISH, CATALAN, TURKISH, BULGARIAN);

	public static List<Locale> values() {
		return values;
	}
}
