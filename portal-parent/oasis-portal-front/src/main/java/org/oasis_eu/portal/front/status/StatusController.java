package org.oasis_eu.portal.front.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.core.dao.CatalogStore;
import org.oasis_eu.portal.core.mongo.dao.my.DashboardRepository;
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

    @Autowired
    private CatalogStore catalogStore;

    @RequestMapping
    public Status status() {
        Status status = new Status();

        status.databaseOk = dashboardRepository.count() != 0;
        status.kernelOk = catalogStore.findApplication("portal") != null;

        return status;
    }

    public static class Status {
        @JsonProperty boolean databaseOk;
        @JsonProperty boolean kernelOk;
    }

}
