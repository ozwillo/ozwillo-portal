package org.oasis_eu.portal.controller.my.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.controller.generic.BaseController;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.portal.services.OrganizationService;
import org.oasis_eu.portal.model.ui.UIOrganization;
import org.oasis_eu.portal.model.dc.DCOrganization;
import org.oasis_eu.portal.model.ui.UIPendingOrganizationMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/my/api/organization")
class OrganizationController extends BaseController {

    @Autowired
    private NetworkService networkService;

    @Autowired
    private OrganizationService organizationService;

    @GetMapping(value = "/search")
    public List<DCOrganization> searchOrganizations(@RequestParam String country_uri, @RequestParam String query) {
        return organizationService.findOrganizations(country_uri, query);
    }

    @GetMapping(value = "/kernel")
    public boolean existsInKernel(@RequestParam String countryUri, String taxRegNumber) {
        return organizationService.existsInKernel(countryUri, taxRegNumber);
    }

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
        return organizationService.create(dcOrganization);
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