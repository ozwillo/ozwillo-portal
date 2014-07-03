package org.oasis_eu.portal.mockserver.provisioning;

import java.util.List;

/**
 * User: schambon
 * Date: 7/1/14
 */
public class InstanceCreated {

    private String instanceId;

    private List<ServiceCreated> services;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public List<ServiceCreated> getServices() {
        return services;
    }

    public void setServices(List<ServiceCreated> services) {
        this.services = services;
    }
}
