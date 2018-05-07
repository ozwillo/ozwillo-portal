package org.oasis_eu.portal.front.my.rest;

import org.oasis_eu.portal.core.model.catalog.ServiceEntry;
import org.oasis_eu.portal.front.generic.BaseController;
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
