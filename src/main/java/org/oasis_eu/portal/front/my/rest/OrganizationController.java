package org.oasis_eu.portal.front.my.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.portal.front.my.services.MyAppsAJAXServices;
import org.oasis_eu.portal.model.appsmanagement.Authority;
import org.oasis_eu.portal.model.appsmanagement.AuthorityType;
import org.oasis_eu.portal.model.appsmanagement.MyAppsInstance;
import org.oasis_eu.portal.model.network.UIOrganization;
import org.oasis_eu.portal.model.network.UIOrganizationMember;
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
            List<MyAppsInstance> instances = appManagementService.getMyInstances(a, true);
            UIOrganization uiOrg = transformToUIOrganization(a);
            uiOrg.setInstances(loadIcons(instances));
            orgs.add(uiOrg);
        }

        return orgs;
    }

    @RequestMapping(value = "/{organizationId}", method = GET)
    public UIOrganization organization(@PathVariable String organizationId) {
        Authority authority = networkService.getOrganizationAuthority(organizationId);
        List<MyAppsInstance> instances = appManagementService.getMyInstances(authority, true);
        List<UIOrganizationMember> members = networkService.getOrganizationMembers(organizationId);

        UIOrganization uiOrg = transformToUIOrganization(authority);
        uiOrg.setInstances(loadIcons(instances));
        uiOrg.setMembers(members);

        return uiOrg;
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


    private List<MyAppsInstance> loadIcons(List<MyAppsInstance> myAppsInstances) {
        for (MyAppsInstance instance : myAppsInstances) {
            instance.setIcon(
                    imageService.getImageForURL(instance.getApplicationInstance().getIcon(RequestContextUtils.getLocale(request)),
                            ImageFormat.PNG_64BY64, false));
            instance.setName(instance.getApplicationInstance().getName(RequestContextUtils.getLocale(request)));
        }

        return myAppsInstances;
    }

    private UIOrganization transformToUIOrganization(Authority authority) {
        UIOrganization uiOrg = new UIOrganization();
        uiOrg.setId(authority.getId());
        uiOrg.setName(authority.getName());
        uiOrg.setAdmin(authority.isAdmin());

        return uiOrg;
    }

}