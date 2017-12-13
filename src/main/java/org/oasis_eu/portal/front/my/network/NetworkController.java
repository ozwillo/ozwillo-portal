package org.oasis_eu.portal.front.my.network;

import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author schambon
 * @author mkalamalami
 */
@Controller
@RequestMapping("/my/network")
public class NetworkController extends PortalController {

    //private static final Logger logger = LoggerFactory.getLogger(NetworkController.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MyNavigationService myNavigationService;


    @ModelAttribute("navigation")
    private List<MyNavigation> getNavigation() {
        return myNavigationService.getNavigation("network");
    }
}
