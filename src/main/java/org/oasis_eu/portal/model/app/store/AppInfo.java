package org.oasis_eu.portal.model.app.store;

import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;

/**
 * User: schambon
 * Date: 6/25/14
 */
public class AppInfo {

    String name, description, paymentOptionDescription;
    String id;
    CatalogEntryType type;
    String icon;

    public AppInfo(String id, String name, String description, String paymentOptionDescription, CatalogEntryType type, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.paymentOptionDescription = paymentOptionDescription;
        this.type = type;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPaymentOptionDescription() {
        return paymentOptionDescription;
    }

    public String getId() {
        return id;
    }

    public CatalogEntryType getType() {
        return type;
    }

    public void setType(CatalogEntryType type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }
}
