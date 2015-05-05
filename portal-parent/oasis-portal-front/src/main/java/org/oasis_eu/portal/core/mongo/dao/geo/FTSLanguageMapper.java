package org.oasis_eu.portal.core.mongo.dao.geo;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to compute MongoDB text language from the source language.
 * We tokenize Bulgarian as if it were Russian, and Catalan as if it were Spanish.
 * Other languages are return "as-is", which may fail if Mongo doesn't support it.
 * User: schambon
 * Date: 4/27/15
 */
public class FTSLanguageMapper {

    private static final Map<String, String> mappings = new HashMap<>();
    static {
        mappings.put("bg", "ru");
        mappings.put("ca", "es");
    }



    public static  String computeMongoLanguage(String source) {
        String lang = mappings.get(source);
        if (lang != null) {
            return lang;
        }
        return source;
    }

}
