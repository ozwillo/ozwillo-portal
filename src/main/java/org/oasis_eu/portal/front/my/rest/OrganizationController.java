package org.oasis_eu.portal.front.my.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.portal.front.my.services.MyAppsAJAXServices;
import org.oasis_eu.portal.model.appsmanagement.Authority;
import org.oasis_eu.portal.model.appsmanagement.MyAppsInstance;
import org.oasis_eu.portal.model.network.UIOrganization;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.portal.services.PortalAppManagementService;
import org.oasis_eu.portal.services.dc.organization.DCOrganization;
import org.oasis_eu.portal.services.dc.organization.OrganizationService;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/my/api/organization")
class OrganizationController extends BaseAJAXServices {

    private static final Logger logger = LoggerFactory.getLogger(MyAppsAJAXServices.class);

    @Autowired
    private PortalAppManagementService appManagementService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ImageService imageService;

    @RequestMapping(value = "", method = GET)
    public List<UIOrganization> organizations() {

        List<Authority> authorities = networkService.getMyAuthorities(true).stream()
                .filter(Authority::isAdmin)
                .collect(Collectors.toList());

        List<UIOrganization> orgs = new ArrayList<>();

        for(Authority a : authorities) {
            List<MyAppsInstance> myInstances = appManagementService.getMyInstances(
                    networkService.getAuthority(a.getType().toString(), a.getId()), true);
            orgs.add(transformToUIOrganization(a, myInstances));
        }

        return orgs;
    }

    @RequestMapping(value = "", method = POST)
    public UIOrganization createOrganization(@RequestBody DCOrganization dcOrganization) {
        return organizationService.create(dcOrganization);
    }

    private UIOrganization transformToUIOrganization(Authority authority, List<MyAppsInstance> myInstances) {
        //Load icons
        for (MyAppsInstance instance : myInstances) {
            instance.setIcon(
                    imageService.getImageForURL(instance.getApplicationInstance().getIcon(RequestContextUtils.getLocale(request)),
                            ImageFormat.PNG_64BY64, false));
            instance.setName(instance.getApplicationInstance().getName(RequestContextUtils.getLocale(request)));
        }

        //Create new UIOrganization
        UIOrganization uiOrg = new UIOrganization();
        uiOrg.setId(authority.getId());
        uiOrg.setName(authority.getName());
        uiOrg.setAdmin(authority.isAdmin());
        uiOrg.setInstances(myInstances);

        return uiOrg;
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