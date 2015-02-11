package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.appstore.ApplicationInstantiationRequest;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance;
import org.oasis_eu.portal.core.model.catalog.Audience;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.model.catalog.PaymentOption;

import java.util.List;
import java.util.Locale;

/**
 * User: schambon
 * Date: 6/24/14
 */
public interface CatalogStore {

    CatalogEntry findApplication(String id);

    CatalogEntry findService(String id);

    List<CatalogEntry> findServicesOfInstance(String instanceId);

    ApplicationInstance findApplicationInstance(String instanceId);

    List<CatalogEntry> findAllVisible(List<Audience> targetAudiences, List<PaymentOption> paymentOptions,
            List<Locale> supportedLocales, List<String> geographicalAreas,
            List<String> categoryIds, String q, String hl, int from);

    void instantiate(String appId, ApplicationInstantiationRequest instancePattern);

    CatalogEntry fetchAndUpdateService(String serviceId, CatalogEntry service);

    void deleteInstance(String instanceId);
}
