package org.oasis_eu.portal.front.my.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.HttpResponse;
import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.portal.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/my/api/acl")
public class AclController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping(value = "")
    @ResponseStatus(HttpStatus.CREATED)
    public void createAcl(@RequestBody Acl acl) {
        applicationService.createAcl(acl.instanceId, acl.user);
    }

    private static class Acl {
        @JsonProperty
        User user;

        @JsonProperty
        String instanceId;
    }

}
