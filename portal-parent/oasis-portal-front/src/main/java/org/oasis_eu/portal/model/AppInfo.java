package org.oasis_eu.portal.model;

/**
 * User: schambon
 * Date: 6/25/14
 */
public class AppInfo {

    String name, description, paymentOptionDescription;
    String id;

    public AppInfo(String id, String name, String description, String paymentOptionDescription) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.paymentOptionDescription = paymentOptionDescription;
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
}
