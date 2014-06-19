package org.oasis_eu.portal.front.my;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.core.mongo.model.my.UserContext;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.AppNotificationData;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.portal.services.PortalDashboardService;
import org.oasis_eu.portal.services.LocalServiceService;
import org.oasis_eu.portal.services.PortalNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Controller
@RequestMapping("/my")
public class MyOzwilloController extends PortalController {

    private static final Logger logger = LoggerFactory.getLogger(MyOzwilloController.class);

    @Autowired
    private PortalDashboardService portalDashboardService;

    @Autowired
    private LocalServiceService localServiceService;

    @Autowired
    private PortalNotificationService notificationService;

    @Autowired
    private MyNavigationService myNavigationService;



    @RequestMapping(method = RequestMethod.GET, value={"/", "", "/dashboard"})
    public String myOzwillo(Model model) {
        List<UserContext> contexts = portalDashboardService.getUserContexts();
        model.addAttribute("contexts", contexts);
        String contextId = contexts.stream().filter(c -> c.isPrimary()).findFirst().get().getId();
        model.addAttribute("contextId", contextId); // current context
        model.addAttribute("entries", portalDashboardService.getDashboardEntries(contextId));
        model.addAttribute("navigation", myNavigationService.getNavigation("dashboard"));
        return "my";
    }

    @RequestMapping(method = RequestMethod.GET, value={"/dashboard/{contextId}"})
    public String dashboard(@PathVariable String contextId, Model model) {
        List<UserContext> contexts = portalDashboardService.getUserContexts();
        model.addAttribute("contexts", contexts);
        model.addAttribute("contextId", contextId); // current context
        model.addAttribute("entries", portalDashboardService.getDashboardEntries(contextId));
        model.addAttribute("navigation", myNavigationService.getNavigation("dashboard"));
        return "my";
    }

    @RequestMapping(method = RequestMethod.GET, value={"/dashboard/{contextId}/fragment"})
    public String dashboardFragment(@PathVariable String contextId, Model model) {
        model.addAttribute("contextId", contextId);
        model.addAttribute("entries", portalDashboardService.getDashboardEntries(contextId));

        return "my::dashboard";
    }


    @RequestMapping(method = RequestMethod.POST, value = "/dashboard")
    public String createDashboard(@RequestParam String dashboardname) {
        UserContext uc = portalDashboardService.createContext(dashboardname);
        return "redirect:/my/dashboard/" + uc.getId();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/dashboard/fragment")
    @ResponseBody
    public String createDashboardFragment(@RequestParam String dashboardname) {
        UserContext uc = portalDashboardService.createContext(dashboardname);
        return uc.getId();
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

    @RequestMapping(method = RequestMethod.GET, value="/api/notifications")
    @ResponseBody
    public NotificationData getNotificationData() {
        return new NotificationData(notificationService.countNotifications());
    }

    @RequestMapping(method = RequestMethod.GET, value="/api/app-notifications/{contextId}")
    @ResponseBody
    public List<AppNotificationData> getAppNotifications(@PathVariable String contextId) {
        List<String> applicationIds = portalDashboardService.getApplicationIds(contextId);
        Map<String, Integer> appNotifs = notificationService.getAppNotifications(applicationIds);

        return applicationIds.stream().map(id -> new AppNotificationData(id, appNotifs.get(id) != null ? appNotifs.get(id) : 0)).collect(Collectors.toList());
    }


    private static class NotificationData {
        int notificationsCount;

        public NotificationData(int notificationsCount) {
            this.notificationsCount = notificationsCount;
        }

        @JsonProperty("notificationsCount")
        public int getNotificationsCount() {
            return notificationsCount;
        }
    }

}
