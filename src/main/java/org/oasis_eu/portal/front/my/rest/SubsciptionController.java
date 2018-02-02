package org.oasis_eu.portal.front.my.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.front.generic.BaseController;
import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.portal.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/my/api/subscription")
public class SubsciptionController extends BaseController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSubscription(@RequestBody Subscription sub) {
        applicationService.subscribeUser(sub.userId, sub.serviceId);
    }

    private static class Subscription {
        @JsonProperty
        String userId;

        @JsonProperty
        String serviceId;
    }
}
