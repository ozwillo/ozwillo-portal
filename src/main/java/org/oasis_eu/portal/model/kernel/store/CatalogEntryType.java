package org.oasis_eu.portal.model.kernel.store;

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
