package org.oasis_eu.portal.core.mongo.model.my;

/**
 * User: schambon
 * Date: 10/30/14
 */
public class UserSubscription {

    private String id;
    private String serviceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
}
