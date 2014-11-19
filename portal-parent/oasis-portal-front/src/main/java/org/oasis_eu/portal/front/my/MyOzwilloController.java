package org.oasis_eu.portal.front.my;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.portal.services.PortalDashboardService;
import org.oasis_eu.portal.services.PortalNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
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
    private PortalNotificationService notificationService;

    @Autowired
    private MyNavigationService myNavigationService;

    @Autowired
    private MessageSource messageSource;

    private static List<String> i18keys = Arrays.asList("create", "switch-dash", "confirm-delete-dash", "confirm-delete-dash-long", "confirm-remove-app", "confirm-remove-app-long");
    private static List<String> generickeys = Arrays.asList("yes", "save", "cancel", "close", "loading", "go", "general-error", "edit", "add", "remove");


    @ModelAttribute("i18n")
    public Map<String, String> getI18n(HttpServletRequest request) throws JsonProcessingException {
        Locale locale = RequestContextUtils.getLocale(request);

        Map<String, String> i18n = new HashMap<>();
        i18n.putAll(i18keys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("my." + k, new Object[]{}, locale))));
        i18n.putAll(generickeys.stream().collect(Collectors.toMap(k -> "ui." + k, k -> messageSource.getMessage("ui." + k, new Object[]{}, locale))));
        return i18n;
    }

    @ModelAttribute("navigation")
    public List<MyNavigation> getNavigation() {
        return myNavigationService.getNavigation("dashboard");
    }

    @RequestMapping(method = RequestMethod.GET, value = {"/", "", "/dashboard"})
    public String show(Model model) {
        return "dashboard/dashboard";
    }


//
//    @RequestMapping(method = RequestMethod.GET, value = {"/dashboard/{contextId}"})
//    public String dashboard(@PathVariable String contextId, Model model) {
//
//        List<UserContext> contexts = portalDashboardService.getUserContexts();
//        model.addAttribute("contexts", contexts);
//        Optional<UserContext> optUserContext = portalDashboardService.getUserContexts().stream().filter(uc -> uc.getId().equals(contextId)).findFirst();
//        if (optUserContext.isPresent()) {
//            model.addAttribute("context", optUserContext.get());
//            model.addAttribute("entries", portalDashboardService.getDashboardEntries(contextId));
//            model.addAttribute("navigation", myNavigationService.getNavigation("dashboard"));
//            return "my";
//        } else {
//            // Invalid dashboard, display default one
//            return "redirect:/my";
//        }
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value={"/dashboard/{contextId}/fragment"})
//    public String dashboardFragment(@PathVariable String contextId, Model model) {
//        model.addAttribute("context", portalDashboardService.getUserContexts().stream().filter(uc -> uc.getId().equals(contextId)).findFirst().get());
//        model.addAttribute("entries", portalDashboardService.getDashboardEntries(contextId));
//        return "my::dashboard";
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value={"/dashboard/{contextId}/fragment/switcher"})
//    public String dashboardSwitcherFragment(@PathVariable String contextId, Model model) {
//        model.addAttribute("contexts", portalDashboardService.getUserContexts());
//        model.addAttribute("context", portalDashboardService.getUserContexts().stream().filter(uc -> uc.getId().equals(contextId)).findFirst().get());
//        return "my::dashboard-switcher";
//    }
//
//    @RequestMapping(method = RequestMethod.POST, value = "/dashboard")
//    public String createDashboard(@RequestParam String dashboardname, Model model) {
//        UserContext uc = portalDashboardService.createContext(dashboardname);
//        model.addAttribute("contexts", portalDashboardService.getUserContexts());
//        model.addAttribute("context", uc);
//        return "my::dashboard-switcher";
//    }
//
//    @RequestMapping(method = RequestMethod.POST, value = "/dashboard/manage")
//    public String manageDashboard(@RequestParam String dashboardid,
//    		@RequestParam String dashboardname,
//    		@RequestParam(required=false, defaultValue="") String delete,
//    		Model model) {
//    	UserContext uc;
//    	if (!StringUtils.isEmpty(delete)) {
//            uc = portalDashboardService.deleteContext(dashboardid);
//    	}
//    	else {
//            uc = portalDashboardService.renameContext(dashboardid, dashboardname);
//    	}
//        model.addAttribute("contexts", portalDashboardService.getUserContexts());
//		model.addAttribute("context", uc);
//        return "my::dashboard-switcher";
//    }
//
//    @RequestMapping(method = RequestMethod.POST, value = "/dashboard/reorder")
//    public String reorderDashboard(@RequestBody DashboardDragDrop dragDrop, Model model) {
//        if (dragDrop.isBefore()) {
//            portalDashboardService.moveBefore(dragDrop.getContextId(), dragDrop.getDraggedId(), dragDrop.getDestId());
//        } else {
//            portalDashboardService.moveAfter(dragDrop.getContextId(), dragDrop.getDraggedId(), dragDrop.getDestId());
//        }
//        model.addAttribute("context", portalDashboardService.getUserContexts().stream().filter(uc -> uc.getId().equals(dragDrop.getContextId())).findFirst().get());
//        model.addAttribute("entries", portalDashboardService.getDashboardEntries(dragDrop.getContextId()));
//
//        return "my::dashboard";
//    }
//
//    @RequestMapping(method = RequestMethod.POST, value = "/dashboard/move_context")
//    public String moveContext(@RequestBody DashboardDragDrop dragDrop, Model model) {
//        portalDashboardService.moveAppToContext(dragDrop.getContextId(), dragDrop.getDraggedId(), dragDrop.getDestId());
//        model.addAttribute("context", portalDashboardService.getUserContexts().stream().filter(uc -> uc.getId().equals(dragDrop.getContextId())).findFirst().get());
//        model.addAttribute("entries", portalDashboardService.getDashboardEntries(dragDrop.getContextId()));
//
//        return "my::dashboard";
//
//    }
//
//    @RequestMapping(method = RequestMethod.GET, value="/notif")
//    public String notifications(Model model, HttpServletRequest request) {
//        model.addAttribute("navigation", myNavigationService.getNavigation("notifications"));
////        model.addAttribute("notifications", notificationService.getNotifications(RequestContextUtils.getLocale(request)));
//        return "my-notif";
//    }
//
//    @ExceptionHandler(TechnicalErrorException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ModelAndView technicalError() {
//        ModelAndView result = new ModelAndView();
//        result.addObject("isAppstore", false);
//        result.addObject("user", user());
//        result.addObject("languages", languages());
//        result.addObject("currentLanguage", currentLanguage());
//        result.addObject("navigation", myNavigationService.getNavigation("dashboard"));
//        result.setViewName("dashboard/dash-error");
//        return result;
//    }
//

}
