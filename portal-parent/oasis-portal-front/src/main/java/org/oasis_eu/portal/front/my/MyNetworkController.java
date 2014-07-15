package org.oasis_eu.portal.front.my;

import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 * @author mkalamalami
 *
 */
@Controller
@RequestMapping("/my/network")
public class MyNetworkController extends PortalController {

    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(MyNetworkController.class);

    @Autowired
    private MyNavigationService myNavigationService;

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping(method = RequestMethod.GET, value="")
    public String profile(Model model) {
        model.addAttribute("navigation", myNavigationService.getNavigation("network"));
        return "my-network";
    }

}