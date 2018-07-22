package org.oasis_eu.portal.model.store;

/**
 * User: schambon
 * Date: 9/26/14
 */
public class AppStoreNavigationStatus {

    String appId = null;
    String appType = null;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public boolean hasApp() {
        return appId != null;
    }
}
