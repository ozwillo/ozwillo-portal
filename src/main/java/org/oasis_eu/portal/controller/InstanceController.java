package org.oasis_eu.portal.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.model.instance.InstanceService;
import org.oasis_eu.portal.model.instance.MyAppsInstance;
import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.portal.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/my/api/instance")
public class InstanceController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/{instanceId}/users")
    public List<User> getUsersOfService(@PathVariable String instanceId) {
        return applicationService.getAllAppUsers(instanceId);
    }

    @GetMapping("/{instanceId}/services")
    public List<InstanceService> getServices(@PathVariable String instanceId){
        return applicationService.getServices(instanceId);
    }

    /**
     * (ideally should be {id}/set-status)
     */
    @PostMapping("/{instanceId}/status")
    public MyAppsInstance setInstanceStatus(@RequestBody MyAppsInstance instance) {
        return applicationService.setInstanceStatus(instance);
    }

    @PostMapping("/{instanceId}/acl")
    @ResponseStatus(HttpStatus.CREATED)
    public void createAcl(@PathVariable String instanceId, @RequestBody AclRequest aclRequest) {
        applicationService.createAcl(instanceId, aclRequest.userId, aclRequest.email);
    }

    @DeleteMapping("/{instanceId}/acl")
    public void deleteAcl(@PathVariable String instanceId, @RequestBody AclRequest aclRequest) {
        applicationService.deleteAcl(instanceId, aclRequest.userId, aclRequest.email);
    }

    private static class AclRequest {
        @JsonProperty
        String userId;

        @JsonProperty
        String email;
    }
}
