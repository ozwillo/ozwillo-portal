package org.oasis_eu.portal.core.model;

import java.lang.management.CompilationMXBean;

/**
 * User: schambon
 * Date: 5/14/14
 */
public class AppStoreHit {
    private HitType type;

    public HitType getType() {
        return type;
    }

    public void setType(HitType type) {
        this.type = type;
    }

    public LocalService getLocalService() {
        return null;
    }

    public Application getApplication() {
        return null;
    }

    public static enum HitType {
        LOCAL_SERVICE, APPLICATION;
    }


}
