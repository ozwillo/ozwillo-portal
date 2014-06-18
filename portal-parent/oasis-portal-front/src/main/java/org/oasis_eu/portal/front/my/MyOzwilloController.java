package org.oasis_eu.portal.front.my;

import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.portal.services.PortalDashboardService;
import org.oasis_eu.portal.services.LocalServiceService;
import org.oasis_eu.portal.services.PortalNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Controller
@RequestMapping("/my")
public class MyOzwilloController extends PortalController {

    @Autowired
    private PortalDashboardService portalDashboardService;

    @Autowired
    private LocalServiceService localServiceService;

    @Autowired
    private PortalNotificationService notificationService;

    @Autowired
    private MyNavigationService myNavigationService;

    @ModelAttribute("notificationsCount")
    public Long countNotifications() {
        return notificationService.countNotifications();
    }

    @RequestMapping(method = RequestMethod.GET, value={"/", "", "/dashboard"})
    public String myOzwillo(Model model, HttpServletRequest request) {
        model.addAttribute("contexts", portalDashboardService.getUserContexts());
        model.addAttribute("entries", portalDashboardService.getDashboardEntries(portalDashboardService.getPrimaryUserContext().getId(), RequestContextUtils.getLocale(request)));
        model.addAttribute("localServices", localServiceService.findLocalServices());
        model.addAttribute("navigation", myNavigationService.getNavigation("dashboard"));
        return "my";
    }

    @RequestMapping(method = RequestMethod.GET, value="/notif")
    public String notifications(Model model, HttpServletRequest request) {
        model.addAttribute("notifications", notificationService.getNotifications(RequestContextUtils.getLocale(request)));
        return "my-notif";
    }

    @RequestMapping(method = RequestMethod.GET, value="/profile")
    public String profile(Model model) {
        model.addAttribute("navigation", myNavigationService.getNavigation("profile"));
        return "my-profile";
    }
}
