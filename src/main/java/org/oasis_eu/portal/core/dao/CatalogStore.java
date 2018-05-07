package org.oasis_eu.portal.core.dao;

import org.oasis_eu.portal.core.model.appstore.ApplicationInstantiationRequest;
import org.oasis_eu.portal.core.model.catalog.*;
import org.oasis_eu.portal.core.model.catalog.ApplicationInstance.InstantiationStatus;

import java.util.List;
import java.util.Locale;

/**
 * User: schambon
 * Date: 6/24/14
 */
public interface CatalogStore {

    CatalogEntry findApplication(String id);

    ServiceEntry findService(String id);

    List<ServiceEntry> findServicesOfInstance(String instanceId);

    /**
     * @param instanceId
     * @return null if none or 403 Forbidden
     */
    ApplicationInstance findApplicationInstance(String instanceId);

    List<CatalogEntry> findAllVisible(List<Audience> targetAudiences, List<PaymentOption> paymentOptions,
        List<Locale> supportedLocales, List<String> geographicalAreas,
        List<String> categoryIds, String q, String hl, int from);

    ApplicationInstance instantiate(String appId, ApplicationInstantiationRequest instancePattern);

    ApplicationInstance findApplicationInstanceOrNull(String instanceId);

    ServiceEntry updateService(String serviceId, ServiceEntry service);

    /**
     * for trash mode
     *
     * @param instanceId
     * @param status
     * @return ApplicationInstance
     */
    ApplicationInstance setInstanceStatus(String instanceId, InstantiationStatus status);

}
