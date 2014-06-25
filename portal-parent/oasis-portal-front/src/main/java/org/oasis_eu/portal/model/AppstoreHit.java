package org.oasis_eu.portal.model;

import org.oasis_eu.portal.core.model.appstore.CatalogEntry;
import org.oasis_eu.portal.core.model.appstore.PaymentOption;

import java.util.Locale;

/**
 * User: schambon
 * Date: 6/25/14
 */
public class AppstoreHit {

    public Locale displayLocale;

    public CatalogEntry catalogEntry;

    public String name, description, providerName;

    private AcquisitionStatus acquisitionStatus;


    public AppstoreHit(Locale displayLocale, CatalogEntry catalogEntry, String providerName, AcquisitionStatus acquisitionStatus) {
        this.displayLocale = displayLocale;
        this.catalogEntry = catalogEntry;
        this.providerName = providerName;
        this.acquisitionStatus = acquisitionStatus;
    }

    public Locale getDisplayLocale() {
        return displayLocale;
    }

    public void setDisplayLocale(Locale displayLocale) {
        this.displayLocale = displayLocale;
    }

    public CatalogEntry getCatalogEntry() {
        return catalogEntry;
    }

    public void setCatalogEntry(CatalogEntry catalogEntry) {
        this.catalogEntry = catalogEntry;
    }

    public String getName() {
        return catalogEntry.getName(displayLocale);
    }

    public String getDescription() {
        return catalogEntry.getDescription(displayLocale);
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public AcquisitionStatus getAcquisitionStatus() {
        return acquisitionStatus;
    }

    public void setAcquisitionStatus(AcquisitionStatus acquisitionStatus) {
        this.acquisitionStatus = acquisitionStatus;
    }


}
