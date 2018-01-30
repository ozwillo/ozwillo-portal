package org.oasis_eu.portal.front.my.rest;

import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.portal.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/my/api/service")
public class ServiceController {

    @Autowired
    private ApplicationService appManagementService;

    @RequestMapping(value = "/{instanceId}/users", method = RequestMethod.GET)
    public List<User> getUsersOfService(@PathVariable String instanceId) {
        return appManagementService.getAllAppUsers(instanceId);
    }

}
