package org.oasis_eu.portal.front.my;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.core.mongo.model.my.UserContext;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.dashboard.AppNotificationData;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.portal.services.PortalDashboardService;
import org.oasis_eu.portal.services.PortalNotificationService;
import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private PortalNotificationService notificationService;

    @Autowired
    private MyNavigationService myNavigationService;

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(method = RequestMethod.GET, value={"/", "", "/dashboard"})
    public String myOzwillo(Model model) {
        List<UserContext> contexts = portalDashboardService.getUserContexts();
        model.addAttribute("contexts", contexts);
        String contextId = contexts.stream().filter(c -> c.isPrimary()).findFirst().get().getId();
        model.addAttribute("context", portalDashboardService.getUserContexts().stream().filter(uc -> uc.getId().equals(contextId)).findFirst().get());
        model.addAttribute("entries", portalDashboardService.getDashboardEntries(contextId));
        model.addAttribute("navigation", myNavigationService.getNavigation("dashboard"));
        return "my";
    }


    @RequestMapping(method = RequestMethod.GET, value = {"/dashboard/{contextId}"})
    public String dashboard(@PathVariable String contextId, Model model) {

        List<UserContext> contexts = portalDashboardService.getUserContexts();
        model.addAttribute("contexts", contexts);
        Optional<UserContext> optUserContext = portalDashboardService.getUserContexts().stream().filter(uc -> uc.getId().equals(contextId)).findFirst();
        if (optUserContext.isPresent()) {
            model.addAttribute("context", optUserContext.get());
            model.addAttribute("entries", portalDashboardService.getDashboardEntries(contextId));
            model.addAttribute("navigation", myNavigationService.getNavigation("dashboard"));
            return "my";
        } else {
            // Invalid dashboard, display default one
            return "redirect:/my";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value={"/dashboard/{contextId}/fragment"})
    public String dashboardFragment(@PathVariable String contextId, Model model) {
        model.addAttribute("context", portalDashboardService.getUserContexts().stream().filter(uc -> uc.getId().equals(contextId)).findFirst().get());
        model.addAttribute("entries", portalDashboardService.getDashboardEntries(contextId));
        return "my::dashboard";
    }

    @RequestMapping(method = RequestMethod.GET, value={"/dashboard/{contextId}/fragment/switcher"})
    public String dashboardSwitcherFragment(@PathVariable String contextId, Model model) {
        model.addAttribute("contexts", portalDashboardService.getUserContexts());
        model.addAttribute("context", portalDashboardService.getUserContexts().stream().filter(uc -> uc.getId().equals(contextId)).findFirst().get());
        return "my::dashboard-switcher";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/dashboard")
    public String createDashboard(@RequestParam String dashboardname, Model model) {
        UserContext uc = portalDashboardService.createContext(dashboardname);
        model.addAttribute("contexts", portalDashboardService.getUserContexts());
        model.addAttribute("context", uc);
        return "my::dashboard-switcher";
    }
    
    @RequestMapping(method = RequestMethod.POST, value = "/dashboard/manage")
    public String manageDashboard(@RequestParam String dashboardid,
    		@RequestParam String dashboardname,
    		@RequestParam(required=false, defaultValue="") String delete,
    		Model model) {
    	UserContext uc;
    	if (!StringUtils.isEmpty(delete)) {
            uc = portalDashboardService.deleteContext(dashboardid);
    	}
    	else {
            uc = portalDashboardService.renameContext(dashboardid, dashboardname);
    	}
        model.addAttribute("contexts", portalDashboardService.getUserContexts());
		model.addAttribute("context", uc);
        return "my::dashboard-switcher";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/dashboard/reorder")
    public String reorderDashboard(@RequestBody DashboardDragDrop dragDrop, Model model) {
        if (dragDrop.isBefore()) {
            portalDashboardService.moveBefore(dragDrop.getContextId(), dragDrop.getDraggedId(), dragDrop.getDestId());
        } else {
            portalDashboardService.moveAfter(dragDrop.getContextId(), dragDrop.getDraggedId(), dragDrop.getDestId());
        }
        model.addAttribute("context", portalDashboardService.getUserContexts().stream().filter(uc -> uc.getId().equals(dragDrop.getContextId())).findFirst().get());
        model.addAttribute("entries", portalDashboardService.getDashboardEntries(dragDrop.getContextId()));

        return "my::dashboard";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/dashboard/move_context")
    public String moveContext(@RequestBody DashboardDragDrop dragDrop, Model model) {
        portalDashboardService.moveAppToContext(dragDrop.getContextId(), dragDrop.getDraggedId(), dragDrop.getDestId());
        model.addAttribute("context", portalDashboardService.getUserContexts().stream().filter(uc -> uc.getId().equals(dragDrop.getContextId())).findFirst().get());
        model.addAttribute("entries", portalDashboardService.getDashboardEntries(dragDrop.getContextId()));

        return "my::dashboard";

    }

    @RequestMapping(method = RequestMethod.GET, value="/notif")
    public String notifications(Model model, HttpServletRequest request) {
        model.addAttribute("navigation", myNavigationService.getNavigation("notifications"));
        model.addAttribute("notifications", notificationService.getNotifications(RequestContextUtils.getLocale(request)));
        return "my-notif";
    }

    @ExceptionHandler(TechnicalErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView technicalError() {
        ModelAndView result = new ModelAndView();
        result.addObject("user", user());
        result.addObject("languages", languages());
        result.addObject("currentLanguage", currentLanguage());
        result.addObject("navigation", myNavigationService.getNavigation("dashboard"));
        result.setViewName("dashboard/dash-error");
        return result;
    }


}
