package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.config.AppStoreNavigationStatus;
import org.oasis_eu.portal.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/{lang}/store")
public class AppStoreController {

    @Autowired
    public UserService userService;

    @RequestMapping(method = RequestMethod.GET, value = {"", "/"})
    public String main(@PathVariable String lang, HttpServletRequest request) {
        if (userService.requiresLogout()) {
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

    @GetMapping(value = "/login")
    public String login(HttpServletRequest request, HttpSession session, @RequestParam(required = false) String appId,
                        @RequestParam(required = false) String appType) {
        AppStoreNavigationStatus status = new AppStoreNavigationStatus();
        if (appId != null && appType != null) {
            status.setAppId(appId);
            status.setAppType(appType);
        }

        session.setAttribute("APP_STORE", status);

        return "redirect:/login?ui_locales=" + RequestContextUtils.getLocale(request);
    }
}
