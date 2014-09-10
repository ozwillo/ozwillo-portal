package org.oasis_eu.portal.front.my.appsmanagement;

import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.portal.services.PortalAppManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String show(Model model, @RequestParam(value = "defaultAuthorityId", required = false, defaultValue = "") String defaultAuthorityId) {
        model.addAttribute("authorities", appManagementService.getMyAuthorities(true));
        model.addAttribute("defaultAuthorityId", defaultAuthorityId);

        return "appsmanagement/appsmanagement";
    }



    @RequestMapping(method=RequestMethod.POST, value="/service-settings/{service_id}")
    public String saveServiceSettings(Model model, @PathVariable("service_id") String serviceId, @ModelAttribute CatalogEntry service) {

        CatalogEntry entry = appManagementService.updateService(serviceId, service);
        return "redirect:/my/appsmanagement?defaultAuthorityId=" + entry.getProviderId();
    }
}
