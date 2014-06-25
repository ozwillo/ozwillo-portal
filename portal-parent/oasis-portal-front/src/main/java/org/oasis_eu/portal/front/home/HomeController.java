package org.oasis_eu.portal.front.home;

import org.oasis_eu.portal.front.generic.PortalController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * User: schambon
 * Date: 5/13/14
 */
@Controller
public class HomeController extends PortalController {

    @RequestMapping("/")
    public String index() {
        return "home";
    }

}
