package org.oasis_eu.portal.front.cms;

import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.services.MyNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: schambon
 * Date: 5/15/14
 */
@Controller
public class ContentController extends PortalController {

    @Autowired
    private MyNavigationService myNavigationService;
    
    @RequestMapping(method = RequestMethod.GET, value="/profile")
    public String profile(Model model) {
        model.addAttribute("navigation", myNavigationService.getNavigation("profile"));
        return "my-profile";
    }

}
