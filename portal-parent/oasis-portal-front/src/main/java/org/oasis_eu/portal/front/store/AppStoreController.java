package org.oasis_eu.portal.front.store;

import org.oasis_eu.portal.config.AppStoreNavigationStatus;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstanceCreationException;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 6/24/14
 */
@Controller
@RequestMapping("/{lang}/store")
public class AppStoreController extends PortalController {

    @Autowired
    private MyNavigationService myNavigationService;

    @Autowired
    private MessageSource messageSource;

    @ModelAttribute("navigation")
    public List<MyNavigation> getNavigation() {
        return myNavigationService.getNavigation(null);
    }

    @Override
    public boolean isAppstore() {
        return true;
    }


    private static final List<String> i18nkeys = Arrays.asList("citizens", "publicbodies", "companies", "free", "paid",
            "languages-supported-by-applications", "look-for-an-application", "keywords",
            "installed", "tos", "privacy", "by", "agree-to-tos", "install", "install_this_app", "confirm-install-this-app", "confirm-install-this-app-paid", "for_myself", "on_behalf_of", "create-new-org", "buying", "sorry", "could-not-install-app", "already-rated", "launch");
    private static final List<String> languagekeys = Arrays.asList("all", "en", "fr", "it", "es", "ca", "tr", "bg"); // OASIS locales
    private static final List<String> generickeys = Arrays.asList("save", "cancel", "ok", "appstore", "close", "loading", "location");
    private static final List<String> networkkeys = Arrays.asList("organization-name", "organization-type", "organization-type.PUBLIC_BODY", "organization-type.COMPANY", "create");

    @ModelAttribute("i18n")
    public Map<String, String> i18n(HttpServletRequest request) {
        Locale locale = RequestContextUtils.getLocale(request);
        Map<String, String> result = new HashMap<String, String>();
        result.putAll(networkkeys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("my.network." + k, new Object[0], locale))));
        result.putAll(i18nkeys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("store." + k, new Object[0], locale))));
        result.putAll(languagekeys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("store.language." + k, new Object[0], locale))));
        result.putAll(generickeys.stream().collect(Collectors.toMap(k -> "ui." + k, k -> messageSource.getMessage("ui." + k, new Object[0], locale))));
        return result;
    }


    @RequestMapping(method = RequestMethod.GET, value = {"", "/"})
    public String main(@PathVariable String lang, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (requiresLogout()) {
            return "redirect:/logout";
        }

        String requestLanguage = RequestContextUtils.getLocale(request).getLanguage();
        if (!lang.equals(requestLanguage)) {
            return "redirect:/" + requestLanguage + "/store";
        }

        model.addAttribute("defaultApp", null);

        return "store/store";
    }


    @RequestMapping(value = {"/service/{serviceId}", "/service/{serviceId}/*"}, method = RequestMethod.GET)
    public String service(@PathVariable String lang, @PathVariable String serviceId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        String requestLanguage = RequestContextUtils.getLocale(request).getLanguage();
        if (!lang.equals(requestLanguage)) {
            return "redirect:/" + requestLanguage + "/store/service/" + serviceId;
        }

        Map<String, String> defaultApp = new HashMap<>();
        defaultApp.put("type", "service");
        defaultApp.put("id", serviceId);

        model.addAttribute("defaultApp", defaultApp);

        return "store/store";
    }

    @RequestMapping(value = {"/application/{applicationId}", "/application/{applicationId}/*"}, method = RequestMethod.GET)
    public String application(@PathVariable String lang, @PathVariable String applicationId, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        String requestLanguage = RequestContextUtils.getLocale(request).getLanguage();
        if (!lang.equals(requestLanguage)) {
            return "redirect:/" + requestLanguage + "/store/application/" + applicationId;
        }

        Map<String, String> defaultApp = new HashMap<>();
        defaultApp.put("type", "application");
        defaultApp.put("id", applicationId);

        model.addAttribute("defaultApp", defaultApp);

        return "store/store";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/login")
    public String login(HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes, @RequestParam(required = false) String appId, @RequestParam(required = false) String appType) {
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
        Map<String, Object> model = new HashMap<String, Object>();
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

