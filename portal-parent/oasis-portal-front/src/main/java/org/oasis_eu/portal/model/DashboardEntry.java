package org.oasis_eu.portal.model;

import org.oasis_eu.portal.core.model.appstore.Application;
import org.oasis_eu.portal.core.model.appstore.LocalService;
import org.oasis_eu.portal.core.model.subscription.Subscription;

import java.util.Locale;

/**
 * Front-end abstraction for a subscription to display in MyOzwillo
 *
 * User: schambon
 * Date: 6/12/14
 */
public class DashboardEntry {

    private Application application;
    private LocalService localService;
    private Subscription subscription;

    private Locale displayLocale;

    // List<Notification> notifications;

    // int ordering -- doesn't necessarily have to live here

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public LocalService getLocalService() {
        return localService;
    }

    public void setLocalService(LocalService localService) {
        this.localService = localService;
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
        return localService != null ? localService.getName(locale) : application.getName(locale);
    }

    public int getNotificationCount() {
        return 0;
    }

    public String getIconId() {
        return "todo";
    }

    public String getDescription(Locale locale) {
        return localService != null ? localService.getDescription(locale) : application.getDescription(locale);
    }

    public String getURL() {
        return localService != null ? localService.getUrl().toExternalForm() : application.getUrl().toExternalForm();
    }

    public void setDisplayLocale(Locale displayLocale) {
        this.displayLocale = displayLocale;
    }
}
