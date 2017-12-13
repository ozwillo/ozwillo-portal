package org.oasis_eu.portal.front.my.appsmanagement;

import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * User: schambon
 * Date: 7/29/14
 */

@Controller
@RequestMapping("/my/apps")
public class MyAppsManagementController extends PortalController {

    @Autowired
    private MyNavigationService navigationService;

    @ModelAttribute("navigation")
    public List<MyNavigation> getNavigation() {
        return navigationService.getNavigation("apps");
    }

}
