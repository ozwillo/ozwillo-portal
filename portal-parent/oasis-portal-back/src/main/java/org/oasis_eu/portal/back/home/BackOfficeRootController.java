package org.oasis_eu.portal.back.home;

import org.oasis_eu.portal.back.generic.BackendController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author mkalamalami
 *
 */
@Controller
public class BackOfficeRootController extends BackendController {

    @RequestMapping("/")
    public String home() {
        return "redirect:/contents";
    }
    
}
