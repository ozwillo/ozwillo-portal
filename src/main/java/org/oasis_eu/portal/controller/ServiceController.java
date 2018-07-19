package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.model.kernel.store.ServiceEntry;
import org.oasis_eu.portal.model.instance.InstanceService;
import org.oasis_eu.portal.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/my/api/service")
public class ServiceController {

    @Autowired
    private ApplicationService applicationService;

    @PutMapping(value = "/{serviceId}")
    public InstanceService saveService(@PathVariable String serviceId, @RequestBody ServiceEntry serviceEntry) {
        return applicationService.updateService(serviceId, serviceEntry);
    }
}
