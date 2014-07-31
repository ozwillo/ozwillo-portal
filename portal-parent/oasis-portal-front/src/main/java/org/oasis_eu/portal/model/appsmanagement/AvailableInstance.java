package org.oasis_eu.portal.model.appsmanagement;

import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.mongo.model.temp.ApplicationInstanceRegistration;
import org.oasis_eu.portal.model.appstore.AppInfo;

import java.util.List;

/**
 * User: schambon
 * Date: 7/29/14
 */
public class AvailableInstance {

    ApplicationInstanceRegistration applicationInstanceRegistration;
    ApplicationInstance applicationInstance;

    List<AvailableService> availableServices;

    public ApplicationInstanceRegistration getApplicationInstanceRegistration() {
        return applicationInstanceRegistration;
    }

    public AvailableInstance setApplicationInstanceRegistration(ApplicationInstanceRegistration applicationInstanceRegistration) {
        this.applicationInstanceRegistration = applicationInstanceRegistration;
        return  this;
    }

    public List<AvailableService> getServices() {
        return availableServices;
    }

    public AvailableInstance setServices(List<AvailableService> availableServices) {
        this.availableServices = availableServices;
        return this;
    }

    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }

    public AvailableInstance setApplicationInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
        return this;
    }
}
