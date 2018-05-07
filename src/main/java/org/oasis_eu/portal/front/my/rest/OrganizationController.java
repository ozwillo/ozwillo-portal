package org.oasis_eu.portal.front.my.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.front.generic.BaseController;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.portal.services.dc.organization.OrganizationService;
import org.oasis_eu.portal.ui.UIOrganization;
import org.oasis_eu.portal.services.dc.organization.DCOrganization;
import org.oasis_eu.portal.ui.UIPendingOrganizationMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/my/api/organization")
class OrganizationController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    @Autowired
    private NetworkService networkService;

    @Autowired
    private OrganizationService organizationService;

    @GetMapping
    public List<UIOrganization> organizations() {
        return networkService.getMyOrganizations();
    }

    @GetMapping(value = "/lazy")
    public List<UIOrganization> getOrganizationsInLazyMode() { return networkService.getMyOrganizationsInLazyMode(); }

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
        return organizationService.create(dcOrganization, true);
    }

    @PutMapping
    public UIOrganization updateDCOrganization(@RequestBody DCOrganization dcOrganization) {
        return organizationService.update(dcOrganization);
    }

    @PutMapping(value = "/{organizationId}/status")
    public UIOrganization setOrganizationStatus(@RequestBody UIOrganization organization) {
        return networkService.setOrganizationStatus(organization);
    }

    @PostMapping ("/invite/{organizationId}")
    public UIPendingOrganizationMember invite(@PathVariable String organizationId, @RequestBody InvitationRequest invitation) {
        return networkService.invite(invitation.email, invitation.admin, organizationId);
    }

    @PutMapping("/{organizationId}/membership/{accountId}/role/{isAdmin}")
    public void updateRoleMember(@PathVariable String organizationId, @PathVariable String accountId,
                             @PathVariable boolean isAdmin) {
        networkService.updateMember(organizationId, accountId, isAdmin);
    }

    @DeleteMapping("/{organizationId}/membership/{accountId}")
    public void removeMember(@PathVariable String organizationId, @PathVariable String accountId) {
        networkService.removeMember(organizationId, accountId);
    }

    @DeleteMapping(value = "/{organizationId}/invitation/{invitationId}")
    public void removeInvitation(@PathVariable String organizationId, @RequestBody UIPendingOrganizationMember member) {
        networkService.removeInvitation(organizationId, member.getId(), member.getPendingMembershipEtag());
    }

    private static class InvitationRequest {
        @JsonProperty
        @NotNull
        @NotEmpty
        String email;

        @JsonProperty
        boolean admin;
    }

}