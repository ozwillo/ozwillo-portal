package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.model.history.OrganizationHistory;
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

    @GetMapping
    public List<OrganizationHistory> getOrganizationHistory() {
        return userOrganizationsHistoryService.getLastVistited();
    }

    @PostMapping(value = "/visit/{dcOrganizationId}")
    public List<OrganizationHistory> postOrganizationActivity(@PathVariable String dcOrganizationId) {
        return userOrganizationsHistoryService.addLastVisited(dcOrganizationId);
    }

    // FIXME : should not return the whole updated list, only a 204 status code
    @DeleteMapping(value = "/delete/{dcOrganizationId}")
    public List<OrganizationHistory> deleteOrganizationHistory(@PathVariable String dcOrganizationId) {
        return userOrganizationsHistoryService.deleteOrganizationHistory(dcOrganizationId);
    }

}
