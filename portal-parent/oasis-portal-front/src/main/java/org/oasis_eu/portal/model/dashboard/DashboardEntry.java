package org.oasis_eu.portal.model.dashboard;

import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.subscription.Subscription;

import java.util.Locale;

/**
 * Front-end abstraction for a subscription to display in MyOzwillo
 *
 * User: schambon
 * Date: 6/12/14
 */
public class DashboardEntry {

    //    private Application application;
//    private LocalService localService;
    private CatalogEntry catalogEntry;
    private Subscription subscription;

    private Locale displayLocale;

    int notificationsCount = 0;

    public CatalogEntry getCatalogEntry() {
        return catalogEntry;
    }

    public void setCatalogEntry(CatalogEntry catalogEntry) {
        this.catalogEntry = catalogEntry;
    }

    public Subscription getSubscription() {

        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public String getName() {
        return getName(displayLocale);
    }

    public String getDescription() {
        return getDescription(displayLocale);
    }


    public String getName(Locale locale) {
        return catalogEntry.getName(locale);
    }

    public int getNotificationsCount() {
        return notificationsCount;
    }

    public void setNotificationsCount(int notificationsCount) {
        this.notificationsCount = notificationsCount;
    }

    public String getIconId() {
        return "todo";
    }

    public String getDescription(Locale locale) {
        return catalogEntry.getDescription(locale);
    }

    public String getURL() {
        return catalogEntry.getUrl();
    }

    public void setDisplayLocale(Locale displayLocale) {
        this.displayLocale = displayLocale;
    }

    public String getApplicationId() {

        return catalogEntry.getId();
    }
}
