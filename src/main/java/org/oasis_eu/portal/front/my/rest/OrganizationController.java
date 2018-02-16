package org.oasis_eu.portal.front.my.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.front.generic.BaseController;
import org.oasis_eu.portal.front.my.services.MyAppsAJAXServices;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.portal.services.dc.organization.OrganizationService;
import org.oasis_eu.portal.ui.UIOrganization;
import org.oasis_eu.portal.services.dc.organization.DCOrganization;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/my/api/organization")
class OrganizationController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MyAppsAJAXServices.class);

    @Autowired
    private NetworkService networkService;

    @Autowired
    private OrganizationService organizationService;

    @GetMapping
    public List<UIOrganization> organizations() {
        return networkService.getMyOrganizations();
    }

    @GetMapping ("/{organizationId}")
    public UIOrganization organization(@PathVariable String organizationId) {
        return networkService.getOrganization(organizationId);
    }

    @GetMapping(value = "/info")
    public DCOrganization getOrganizationInfo(@RequestParam String dcId) {
        return organizationService.getOrganization(dcId);
    }

    @PostMapping
    public UIOrganization createOrganization(@RequestBody DCOrganization dcOrganization) {
        return organizationService.create(dcOrganization);
    }

    @PutMapping
    public UIOrganization updateDCOrganization(@RequestBody DCOrganization dcOrganization) {
        return organizationService.update(dcOrganization);
    }

    @PostMapping ("/invite/{organizationId}")
    public void invite(@PathVariable String organizationId, @RequestBody InvitationRequest invitation, Errors errors) {
        logger.debug("Inviting {} to organization {}", invitation.email, organizationId);

        if (errors.hasErrors()) {
            throw new WrongQueryException();
        }

        networkService.invite(invitation.email, organizationId);
    }

    @DeleteMapping("/{organizationId}/membership/{accountId}")
    public void removeMember(@PathVariable String organizationId, @PathVariable String accountId) {
        networkService.removeMember(organizationId, accountId);
    }

    private static class InvitationRequest {
        @JsonProperty
        @NotNull
        @NotEmpty
        String email;
    }

}