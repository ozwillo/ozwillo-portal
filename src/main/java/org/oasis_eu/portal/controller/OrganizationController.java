package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.model.dc.DCOrganization;
import org.oasis_eu.portal.model.kernel.organization.UserMembership;
import org.oasis_eu.portal.model.organization.InvitationRequest;
import org.oasis_eu.portal.model.organization.UIOrganization;
import org.oasis_eu.portal.model.organization.UIOrganizationMember;
import org.oasis_eu.portal.model.organization.UIPendingOrganizationMember;
import org.oasis_eu.portal.services.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/my/api/organization")
class OrganizationController {

    private final OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

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
        return organizationService.getMyOrganizations();
    }

    @GetMapping("/{organizationId}")
    public UIOrganization organization(@PathVariable String organizationId) {
        return organizationService.getOrganizationFromKernel(organizationId);
    }

    @GetMapping("/light/{organizationId}")
    public UIOrganization getLightOrganization(@PathVariable String organizationId) {
        return organizationService.getOrganizationFromKernelWithoutInstances(organizationId);
    }

    @GetMapping(value = "/info")
    public DCOrganization getOrganizationInfo(@RequestParam String dcId) {
        return organizationService.getOrganization(dcId);
    }

    @GetMapping("/{organizationId}/members")
    public List<UIOrganizationMember> organizationMember(@PathVariable String organizationId) {
        return organizationService.getOrganizationMembers(organizationId, false);
    }

    @GetMapping("/{organizationId}/accepted-members")
    public List<UIOrganizationMember> acceptedOrganizationMembers(@PathVariable String organizationId) {
        return organizationService.getOrganizationMembers(organizationId, true);
    }

    @GetMapping(value = "/memberships")
    public List<UserMembership> memberships(@RequestParam String query) {
        return organizationService.searchUserMembershipsFromQuery(query);
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
        return organizationService.setOrganizationStatus(organization);
    }

    @PostMapping("/invite/{organizationId}")
    public UIPendingOrganizationMember invite(@PathVariable String organizationId, @RequestBody InvitationRequest invitation) {
        return organizationService.invite(invitation.getEmail(), invitation.isAdmin(), organizationId);
    }

    @PostMapping("/invite/multiple/{organizationId}")
    public List<UIPendingOrganizationMember> invite(@PathVariable String organizationId, @RequestBody List<InvitationRequest> invitations) {
        return organizationService.inviteMultiple(invitations,organizationId);
    }


    @DeleteMapping(value = "/{organizationId}/invitation/{invitationId}")
    public void removeInvitation(@PathVariable String organizationId, @RequestBody UIPendingOrganizationMember member) {
        organizationService.removeInvitation(organizationId, member.getId(), member.getPendingMembershipEtag());
    }

    @PutMapping("/{organizationId}/membership/{accountId}/role/{isAdmin}")
    public void updateRoleMember(@PathVariable String organizationId, @PathVariable String accountId,
                                 @PathVariable boolean isAdmin) {
        organizationService.updateMember(organizationId, accountId, isAdmin);
    }

    @DeleteMapping("/{organizationId}/membership/{accountId}")
    public void removeMember(@PathVariable String organizationId, @PathVariable String accountId) {
        organizationService.removeMember(organizationId, accountId);
    }



}