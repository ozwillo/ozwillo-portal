package org.oasis_eu.portal.core.dao;

import java.util.List;
import java.util.Locale;

import org.oasis_eu.portal.core.model.appstore.ApplicationInstantiationRequest;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance.InstantiationStatus;
import org.oasis_eu.portal.core.model.catalog.Audience;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.catalog.PaymentOption;

/**
 * User: schambon
 * Date: 6/24/14
 */
public interface CatalogStore {

    CatalogEntry findApplication(String id);

    CatalogEntry findService(String id);

    List<CatalogEntry> findServicesOfInstance(String instanceId);

    /**
     * 
     * @param instanceId
     * @return null if none or 403 Forbidden
     */
    ApplicationInstance findApplicationInstance(String instanceId);

    List<CatalogEntry> findAllVisible(List<Audience> targetAudiences, List<PaymentOption> paymentOptions,
            List<Locale> supportedLocales, List<String> geographicalAreas,
            List<String> categoryIds, String q, String hl, int from);

    void instantiate(String appId, ApplicationInstantiationRequest instancePattern);

    CatalogEntry fetchAndUpdateService(String serviceId, CatalogEntry service);

    /**
     * for trash mode
     * @param instanceId
     * @param status
     * @return optional (error, warning...) message returned by Kernel
     */
    String setInstanceStatus(String instanceId, InstantiationStatus status);
    
}
