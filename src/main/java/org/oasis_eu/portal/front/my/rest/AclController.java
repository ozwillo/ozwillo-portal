package org.oasis_eu.portal.front.my.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.front.generic.BaseController;
import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.portal.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/my/api/acl")
public class AclController extends BaseController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createAcl(@RequestBody AclRequest aclRequest) {
        applicationService.createAcl(aclRequest.instanceId, aclRequest.user);
    }

    @DeleteMapping
    public void deleteAcl(@RequestBody AclRequest aclRequest) {
        applicationService.deleteAcl(aclRequest.instanceId, aclRequest.user);
    }

    private static class AclRequest {
        @JsonProperty
        User user;

        @JsonProperty
        String instanceId;
    }

}
