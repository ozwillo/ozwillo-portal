package org.oasis_eu.portal.back.home;

import org.oasis_eu.portal.core.controller.PortalController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author mkalamalami
 *
 */
@Controller
public class HomeController extends PortalController {

    @RequestMapping("/")
    public String home() {
        return "redirect:/contents";
    }
    
}
