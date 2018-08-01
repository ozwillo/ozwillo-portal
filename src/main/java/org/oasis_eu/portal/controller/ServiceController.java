package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.model.kernel.instance.Subscription;
import org.oasis_eu.portal.model.kernel.store.ServiceEntry;
import org.oasis_eu.portal.model.instance.InstanceService;
import org.oasis_eu.portal.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/my/api/service")
public class ServiceController {

    private final ApplicationService applicationService;

    @Autowired
    public ServiceController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PutMapping(value = "/{serviceId}")
    public InstanceService saveService(@PathVariable String serviceId, @RequestBody ServiceEntry serviceEntry) {
        return applicationService.updateService(serviceId, serviceEntry);
    }

    @PostMapping("/{serviceId}/subscription/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Subscription createSubscription(@PathVariable String serviceId, @PathVariable String userId) {
        return applicationService.subscribeUser(userId, serviceId);
    }

    @DeleteMapping("/{serviceId}/subscription/{userId}")
    public void deleteSubscription(@PathVariable String serviceId, @PathVariable String userId) {
        applicationService.unsubscribeUser(userId, serviceId);
    }
}
