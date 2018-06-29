package org.oasis_eu.portal.front.my.rest;

import org.oasis_eu.portal.front.generic.BaseController;
import org.oasis_eu.portal.model.app.instance.MyAppsInstance;
import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.portal.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/my/api/instance")
public class InstanceController extends BaseController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/{instanceId}/users")
    public List<User> getUsersOfService(@PathVariable String instanceId) {
        return applicationService.getAllAppUsers(instanceId);
    }

    /**
     * (ideally should be {id}/set-status)
     *
     * @param instance
     * @return
     */
    @PostMapping("/{instanceId}/status")
    public MyAppsInstance setInstanceStatus(@RequestBody MyAppsInstance instance) {
        return applicationService.setInstanceStatus(instance);
    }

}
