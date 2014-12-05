package org.oasis_eu.portal.front.my.appsmanagement;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 7/29/14
 */

@Controller
@RequestMapping("/my/appsmanagement")
public class MyAppsManagementController extends PortalController {

    private static List<String> i18keys = Arrays.asList(
            "none", "manage_users", "users", "settings", "name", "actions",
            "settings-add-a-user", "description", "icon", "published", "notpublished", "services",
            "restricted-service");

    private static List<String> generickeys = Arrays.asList("save", "cancel", "loading", "delete");

    @Autowired
    private MyNavigationService navigationService;

    @Autowired
    private MessageSource messageSource;


    @ModelAttribute("navigation")
    public List<MyNavigation> getNavigation() {
        return navigationService.getNavigation("appsmanagement");
    }

    @ModelAttribute("i18n")
    public Map<String, String> getI18n(HttpServletRequest request) throws JsonProcessingException {
        Locale locale = RequestContextUtils.getLocale(request);

        Map<String, String> i18n = new HashMap<>();
        i18n.putAll(i18keys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("my.apps." + k, new Object[]{}, locale))));
        i18n.putAll(generickeys.stream().collect(Collectors.toMap(k -> "ui." + k, k -> messageSource.getMessage("ui." + k, new Object[]{}, locale))));
        return i18n;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String show() {

        if (requiresLogout()) {
            return "redirect:/logout";
        }
        return "appmanagement/myapps";
    }

}
