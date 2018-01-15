package org.oasis_eu.portal.front.my.rest;

import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.portal.front.my.services.MyAppsAJAXServices;
import org.oasis_eu.portal.model.appsmanagement.Authority;
import org.oasis_eu.portal.model.appsmanagement.MyAppsInstance;
import org.oasis_eu.portal.model.network.UIOrganization;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.portal.services.PortalAppManagementService;
import org.oasis_eu.portal.services.dc.organization.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/my/api/organization")
class OrganizationController extends BaseAJAXServices {

    private static final Logger logger = LoggerFactory.getLogger(MyAppsAJAXServices.class);

    @Autowired
    private PortalAppManagementService appManagementService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "", method = GET)
    public List<UIOrganization>  organizations() {

        List<Authority> authorities = networkService.getMyAuthorities(true).stream()
                .filter(Authority::isAdmin)
                //.map(a -> new Authority(a.getType(), a.getName(), a.getType() + "::" + a.getId(), a.isAdmin()))
                .collect(Collectors.toList());

        List<UIOrganization> orgs = new ArrayList<>();

        for(Authority a : authorities) {
            String authorityId = a.getId();
            //String[] strings = authorityId.split("::");

            List<MyAppsInstance> myInstances =
                    //appManagementService.getMyInstances(networkService.getAuthority(strings[0], strings[1]), true);
                    appManagementService.getMyInstances(networkService.getAuthority(a.getType().toString(), a.getId()), true);
            orgs.add(transformToUIOrganization(a, myInstances));
        }

        return orgs;
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




}