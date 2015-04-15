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


    @ModelAttribute("notif_i18n")
    public Map<String, String> getNotifI18n(HttpServletRequest request) {

        List<String> keys = Arrays.asList("ui.notifications", "notif.date", "notif.app", "notif.message", "notif.archive", "notif.manage", "notif.no-notification", "notif.unread", "notif.read", "notif.any", "notif.all-apps");
        Locale locale = RequestContextUtils.getLocale(request);

        return keys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage(k, new Object[0], locale)));

    }

    @ModelAttribute("navigation")
    public List<MyNavigation> getNavigation() {
        return myNavigationService.getNavigation("dashboard");
    }

    @RequestMapping(method = RequestMethod.GET, value = {"/", "", "/dashboard"})
    public String show(Model model) {
        if (requiresLogout()) {
            return "redirect:/logout";
        }
        return "dashboard/dashboard";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/notif")
    public String notifications(Model model, HttpServletRequest request) {
        model.addAttribute("navigation", myNavigationService.getNavigation("notifications"));
        return "my-notif";
    }

}
