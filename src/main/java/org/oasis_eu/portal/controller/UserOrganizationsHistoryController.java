package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.model.dc.DCOrganization;
import org.oasis_eu.portal.model.store.OrganizationHistory;
import org.oasis_eu.portal.services.UserOrganizationsHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/my/api/organizationHistory")
public class UserOrganizationsHistoryController {

    private final UserOrganizationsHistoryService userOrganizationsHistoryService;

    @Autowired
    public UserOrganizationsHistoryController(UserOrganizationsHistoryService userOrganizationsHistoryService) {
        this.userOrganizationsHistoryService = userOrganizationsHistoryService;
    }

    @GetMapping(value = "")
    public List<OrganizationHistory> getOrganizationHistory() {
        return userOrganizationsHistoryService.getLastVistited();
    }

    @PostMapping(value = "")
    public List<OrganizationHistory> postOrganizationActivity(@RequestBody DCOrganization dcOrganization) {
        return userOrganizationsHistoryService.addLastVisited(dcOrganization.getId());
    }

}
