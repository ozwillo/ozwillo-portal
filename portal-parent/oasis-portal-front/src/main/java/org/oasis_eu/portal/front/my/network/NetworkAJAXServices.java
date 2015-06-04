package org.oasis_eu.portal.front.my.network;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.portal.model.network.UIOrganization;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User: schambon Date: 10/24/14
 */
@RestController
@RequestMapping("/my/api/network")
public class NetworkAJAXServices extends BaseAJAXServices {

    private static final Logger logger = LoggerFactory.getLogger(NetworkAJAXServices.class);

    @Value("${application.devmode:false}")
    private boolean devmode;

    @Autowired
    private NetworkService networkService;

    @RequestMapping(value = "/organizations", method = GET)
    public List<UIOrganization> organizations() {
        List<UIOrganization> organizations = networkService.getMyOrganizations();

        logger.debug("Found organizations: {}", organizations);

        return organizations;
    }

    @RequestMapping(value = "/organization/{organizationId}", method = POST)
    public void updateOrganization(@RequestBody @Valid UIOrganization organization, Errors errors) {
        logger.debug("Updating organization {}", organization);

        networkService.updateOrganization(organization);
    }

    @RequestMapping(value = "/invite/{organizationId}", method = POST)
    public void invite(@PathVariable String organizationId, @RequestBody @Valid InvitationRequest invitation,
            Errors errors) {
        logger.debug("Inviting {} to organization {}", invitation.email, organizationId);

        if (errors.hasErrors()) {
            throw new WrongQueryException();
        }

        networkService.invite(invitation.email, organizationId);

    }

    @RequestMapping(value = "/remove-invitation/{organizationId}", method = POST)
    public void removeInvitation(@PathVariable String organizationId,
            @RequestBody @Valid RemoveInvitationRequest invitation, Errors errors) {
        logger.debug("Removing invitation {} to organization {}", invitation.email, organizationId);

        if (errors.hasErrors()) {
            throw new WrongQueryException();
        }

        networkService.removeInvitation(organizationId, invitation.id, invitation.etag);

    }

    @RequestMapping(value = "/leave", method = POST)
    public void leave(@RequestBody @Valid LeaveRequest request) {
        logger.debug("Leaving {}", request.organization);

        networkService.leave(request.organization);
    }

    @RequestMapping(value = "/create-organization", method = POST)
    public UIOrganization createOrganization(@RequestBody CreateOrganizationRequest createOrganizationRequest) {
        logger.debug("Creating organization {} of type {}", createOrganizationRequest.name,
                createOrganizationRequest.type, createOrganizationRequest.territoryId);

        return networkService.createOrganization(createOrganizationRequest.name, createOrganizationRequest.type,
                createOrganizationRequest.territoryId);

    }

    @RequestMapping(value = "/organization/{organizationId}/set-status", method = POST)
    public String setOrganizationStatus(@RequestBody @Valid UIOrganization organization, Errors errors) {
        logger.debug("Updating organization {}", organization);

        return networkService.setOrganizationStatus(organization);
    }

    public static class InvitationRequest {
        @JsonProperty
        @NotNull
        @NotEmpty
        String email;
    }

    public static class RemoveInvitationRequest {
        @JsonProperty
        @NotNull
        @NotEmpty
        String id;

        @JsonProperty
        @NotNull
        @NotEmpty
        String email;

        @JsonProperty
        @NotNull
        @NotEmpty
        String etag;
    }

    public static class LeaveRequest {
        @JsonProperty
        @NotNull
        @NotEmpty
        String organization;
    }

    public static class CreateOrganizationRequest {
        @JsonProperty
        @NotNull
        @NotEmpty
        String name;
        @JsonProperty
        @NotNull
        @NotEmpty
        String type;
        @JsonProperty("territory_id")
        // @NotNull // TODO
        // @NotEmpty // TODO
        URI territoryId;
    }
}
