package org.oasis_eu.portal.front.my.profile;

import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/my/profile")
public class ProfileController extends PortalController {

    @Autowired
    private MyNavigationService myNavigationService;


    @ModelAttribute("navigation")
    private List<MyNavigation> getNavigation() {
        return myNavigationService.getNavigation("profile");
    }


    @RequestMapping(method = RequestMethod.GET, value = "/franceconnect")
    public String synchronizeProfileToFranceConnect() throws ExecutionException {
        if (requiresLogout()) {
            return "redirect:/logout";
        }
        return "profile/my-synchronize-fc-profile";
    }
}
