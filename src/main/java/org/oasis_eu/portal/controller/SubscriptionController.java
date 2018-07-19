package org.oasis_eu.portal.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.model.kernel.instance.Subscription;
import org.oasis_eu.portal.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/my/api/subscription")
public class SubscriptionController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Subscription createSubscription(@RequestBody RequestSubscription sub) {
        return applicationService.subscribeUser(sub.userId, sub.serviceId);
    }


    @DeleteMapping
    public void deleteSubscription(@RequestBody RequestSubscription sub) {
        applicationService.unsubscribeUser(sub.userId, sub.serviceId);
    }

    private static class RequestSubscription {
        @JsonProperty
        String userId;

        @JsonProperty
        String serviceId;
    }
}
