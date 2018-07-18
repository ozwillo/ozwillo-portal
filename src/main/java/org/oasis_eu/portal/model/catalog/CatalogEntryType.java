package org.oasis_eu.portal.model.catalog;

/**
 * User: schambon
 * Date: 6/24/14
 */
public enum CatalogEntryType {
    APPLICATION,
    SERVICE;

    public static CatalogEntryType of(String input) {
        return valueOf(input.toUpperCase());
    }

}
