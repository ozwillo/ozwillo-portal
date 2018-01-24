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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

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

    @RequestMapping(value = "", method = GET)
    public List<UIOrganization> organizations() {
        return networkService.getMyOrganizations();
    }

    @RequestMapping(value = "/{organizationId}", method = GET)
    public UIOrganization organization(@PathVariable String organizationId) {
        return networkService.getOrganization(organizationId);
    }

    @RequestMapping(value = "", method = POST)
    public UIOrganization createOrganization(@RequestBody DCOrganization dcOrganization) {
        return organizationService.create(dcOrganization);
    }

    @RequestMapping(value = "/invite/{organizationId}", method = POST)
    public void invite(@PathVariable String organizationId, @RequestBody InvitationRequest invitation, Errors errors) {
        logger.debug("Inviting {} to organization {}", invitation.email, organizationId);

        if (errors.hasErrors()) {
            throw new WrongQueryException();
        }

        networkService.invite(invitation.email, organizationId);
    }


    private static class InvitationRequest {
        @JsonProperty
        @NotNull
        @NotEmpty
        String email;
    }

}