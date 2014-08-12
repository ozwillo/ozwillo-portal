package org.oasis_eu.portal.front.my;

import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.model.appsmanagement.Authority;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.portal.services.PortalAppManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * User: schambon
 * Date: 7/29/14
 */

@Controller
@RequestMapping("/my/appsmanagement")
public class MyAppsManagementController extends PortalController {

    @Autowired
    private MyNavigationService navigationService;

    @Autowired
    private PortalAppManagementService appManagementService;

    @ModelAttribute("navigation")
    public List<MyNavigation> getNavigation() {
        return navigationService.getNavigation("appsmanagement");
    }


    @RequestMapping(method = RequestMethod.GET, value ={"","/"})
    public String show(Model model) {
        model.addAttribute("authorities", appManagementService.getMyAuthorities());

        return "my-appsmanagement";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/authority/{authority_type}/{authority_id}")
    public String getAuthority(Model model, @PathVariable("authority_type") String authorityType, @PathVariable("authority_id") String authorityId) {
        Authority authority = appManagementService.getAuthority(authorityType, authorityId);

        if (authority == null) {
            return "my-apps-byauth::forbidden";
        }

        model.addAttribute("authority", authority);
        model.addAttribute("instances", appManagementService.getMyInstances(authority));

        return "my-apps-byauth::authority";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/settings/{app_id}")
    public String appSettings(Model model, @PathVariable("app_id") String instanceId) {
        model.addAttribute("instance", appManagementService.getInstance(instanceId)); // note that we trust the Kernel's security on that one

        return "appsmanagement/instance-settings::main";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/settings/{app_id}/{service_id}")
    public String serviceSettings(Model model, @PathVariable("app_id") String instanceId, @PathVariable("service_id") String serviceId) {
        return "appsmanagement/service-settings::main";
    }
}
