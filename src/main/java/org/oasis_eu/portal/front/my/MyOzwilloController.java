package org.oasis_eu.portal.front.my;

import org.oasis_eu.portal.front.generic.PortalController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Controller
@RequestMapping("/my")
public class MyOzwilloController extends PortalController {

    @RequestMapping(method = RequestMethod.GET, value = "/**")
    public String show() {
        if (requiresLogout()) {
            return "redirect:/logout";
        }
        return "index";
    }

}
