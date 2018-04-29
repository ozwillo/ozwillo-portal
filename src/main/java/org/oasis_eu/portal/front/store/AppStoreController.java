package org.oasis_eu.portal.front.store;

import org.oasis_eu.portal.config.AppStoreNavigationStatus;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstanceCreationException;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: schambon
 * Date: 6/24/14
 */
@Controller
@RequestMapping("/{lang}/store")
public class AppStoreController extends PortalController {

    @Autowired
    private MyNavigationService myNavigationService;

    @ModelAttribute("navigation")
    public List<MyNavigation> getNavigation() {
        return myNavigationService.getNavigation(null);
    }

    @Override
    public boolean isAppstore() {
        return true;
    }


    @RequestMapping(method = RequestMethod.GET, value = {"", "/"})
    public String main(@PathVariable String lang, HttpServletRequest request) {
        if (requiresLogout()) {
            return "redirect:/logout";
        }

        String requestLanguage = RequestContextUtils.getLocale(request).getLanguage();
        if (requestLanguage.isEmpty()) {
            // happens ex. on Firefox private navigation on first time
            requestLanguage = "en"; // switch to default, else redirects to http://store
            // (don't merely keep lang, might be outside accepted languages)
        }
        if (!lang.equals(requestLanguage)) {
            return "redirect:/" + requestLanguage + "/store";
        }

        return "index";
    }


    @RequestMapping(value = {"/service/{serviceId}", "/service/{serviceId}/*"}, method = RequestMethod.GET)
    public String service(@PathVariable String lang, @PathVariable String serviceId, HttpServletRequest request) {
        String requestLanguage = RequestContextUtils.getLocale(request).getLanguage();
        if (!lang.equals(requestLanguage)) {
            return "redirect:/" + requestLanguage + "/store/service/" + serviceId;
        }

        return "index";
    }

    @RequestMapping(value = {"/application/{applicationId}", "/application/{applicationId}/*"}, method = RequestMethod.GET)
    public String application(@PathVariable String lang, @PathVariable String applicationId, HttpServletRequest request) {
        String requestLanguage = RequestContextUtils.getLocale(request).getLanguage();
        if (!lang.equals(requestLanguage)) {
            return "redirect:/" + requestLanguage + "/store/application/" + applicationId;
        }

        return "index";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/login")
    public String login(HttpServletRequest request, HttpSession session, @RequestParam(required = false) String appId, @RequestParam(required = false) String appType) {
        AppStoreNavigationStatus status = new AppStoreNavigationStatus();
        if (appId != null && appType != null) {
            status.setAppId(appId);
            status.setAppType(appType);
        }

        session.setAttribute("APP_STORE", status);

        return "redirect:/login?ui_locales=" + RequestContextUtils.getLocale(request);
    }


    @ExceptionHandler(ApplicationInstanceCreationException.class)
    public ModelAndView instantiationError(ApplicationInstanceCreationException e) {
        Map<String, Object> model = new HashMap<>();
        model.put("appname", e.getRequested().getName());
        model.put("appid", e.getApplicationId());
        model.put("errortype", e.getType().toString());
        model.put("isAppstore", Boolean.TRUE);

        model.put("navigation", myNavigationService.getNavigation(null));
        model.put("currentLanguage", currentLanguage());
        model.put("user", user());

        return new ModelAndView("store/instantiation-error", model);
    }

}
