package org.oasis_eu.portal.controller.my.rest;

import org.oasis_eu.portal.model.catalog.ServiceEntry;
import org.oasis_eu.portal.controller.generic.BaseController;
import org.oasis_eu.portal.model.app.service.InstanceService;
import org.oasis_eu.portal.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/my/api/service")
public class ServiceController extends BaseController {

    @Autowired
    private ApplicationService applicationService;

    @PutMapping(value = "/{serviceId}")
    public InstanceService saveService(@PathVariable String serviceId, @RequestBody ServiceEntry serviceEntry) {
        return applicationService.updateService(serviceId, serviceEntry);
    }
}
