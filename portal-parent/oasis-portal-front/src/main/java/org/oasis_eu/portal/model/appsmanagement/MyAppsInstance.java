package org.oasis_eu.portal.model.appsmanagement;

import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.model.appstore.AppInfo;

import java.util.List;

/**
 * User: schambon
 * Date: 7/29/14
 */
public class MyAppsInstance {

    ApplicationInstance applicationInstance;
    AppInfo application;

    List<MyAppsService> myAppsServices;
    String icon;


    public List<MyAppsService> getServices() {
        return myAppsServices;
    }

    public MyAppsInstance setServices(List<MyAppsService> myAppsServices) {
        this.myAppsServices = myAppsServices;
        return this;
    }

    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }

    public MyAppsInstance setApplicationInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
        return this;
    }

    public AppInfo getApplication() {
        return application;
    }

    public MyAppsInstance setApplication(AppInfo application) {
        this.application = application;
        return this;
    }

    public String getName() {
        return application != null ? application.getName() : "--";
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public String getId() {
        return applicationInstance.getInstanceId();
    }
}
