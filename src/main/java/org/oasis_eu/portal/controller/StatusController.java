package org.oasis_eu.portal.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.dao.portal.DashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User: schambon
 * Date: 10/17/14
 */
@RestController
@RequestMapping("/status")
public class StatusController {

    @Autowired
    private DashboardRepository dashboardRepository;

    @RequestMapping
    public Status status() {
        Status status = new Status();

        status.databaseOk = dashboardRepository.count() != 0;

        return status;
    }

    public static class Status {
        @JsonProperty
        boolean databaseOk;
    }

}
