package org.oasis_eu.portal.front.store;

import org.oasis_eu.portal.config.AppStoreNavigationStatus;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstanceCreationException;
import org.oasis_eu.portal.core.model.catalog.Audience;
import org.oasis_eu.portal.core.model.catalog.CatalogEntryType;
import org.oasis_eu.portal.core.model.catalog.PaymentOption;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.model.appstore.AppstoreHit;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.portal.services.PortalAppManagementService;
import org.oasis_eu.portal.services.PortalAppstoreService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
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

    @Autowired
    private PortalAppstoreService appstoreService;

    @Autowired
    private PortalAppManagementService appManagementService;

    @ModelAttribute("navigation")
    public List<MyNavigation> getNavigation() {
        return myNavigationService.getNavigation(null);
    }

    @Autowired
    private UserInfoService userInfoHelper;

    @Override
    public boolean isAppstore() {
        return true;
    }

    @RequestMapping(method = RequestMethod.GET, value = {"", "/"})
    public String main(@PathVariable String lang, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        String requestLanguage = RequestContextUtils.getLocale(request).getLanguage();
        if (! lang.equals(requestLanguage)) {
            return "redirect:/" + requestLanguage + "/store";
        }

        model.addAttribute("hits", appstoreService.getAll(Arrays.asList(Audience.values()), Arrays.asList(PaymentOption.values())));

        return "store/appstore";
    }

    @RequestMapping(method = RequestMethod.GET, value="/login")
    public String login(HttpSession session, RedirectAttributes redirectAttributes, @RequestParam(required = false) String appId, @RequestParam(required = false) String appType) {
        AppStoreNavigationStatus status = new AppStoreNavigationStatus();
        if (appId != null && appType != null) {
            status.setAppId(appId);
            status.setAppType(appType);
        }

        session.setAttribute("APP_STORE", status);

        return "redirect:/login";
    }


    @RequestMapping(method = RequestMethod.POST, value="/search")
    public String search(Model model, @RequestParam(required = false) String query, @RequestParam List<Audience> audience, @RequestParam List<PaymentOption> paymentOptions) {
        model.addAttribute("hits", appstoreService.getAll(audience, paymentOptions));

        return "store/appstore::hits";
    }

    @RequestMapping(method = RequestMethod.POST, value="/buy")
    public String buy(@RequestParam String appId, @RequestParam CatalogEntryType appType, @RequestParam(required = false) String organizationId) {
        appstoreService.buy(appId, appType, organizationId);
        return "redirect:/my/dashboard";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/application/{appId}/{appType}")
    public String application(@PathVariable String appId, @PathVariable String appType, @RequestParam(required = false) boolean fromAuth, Model model) {
        AppstoreHit info = appstoreService.getInfo(appId, CatalogEntryType.valueOf(appType));
        model.addAttribute("app", info);
        if (userInfoHelper.isAuthenticated()) {
            model.addAttribute("authorities", appManagementService.getMyAuthorities(false));
        }

        if (fromAuth && info.isOnlyCitizens()) {
            return buy(appId, CatalogEntryType.valueOf(appType), null);
        }

        if (fromAuth) {
            model.addAttribute("openPopover", true);
        } else {
            model.addAttribute("openPopover", false);
        }

        return "store/application";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/application/{appId}/{appType}/inner")
    public String applicationInner(@PathVariable String appId, @PathVariable String appType, Model model) {
        model.addAttribute("app", appstoreService.getInfo(appId, CatalogEntryType.valueOf(appType)));
        if (userInfoHelper.isAuthenticated()) {
            model.addAttribute("authorities", appManagementService.getMyAuthorities(false));
        }
        model.addAttribute("openPopover", false);
        return "store/application::content";
    }


    @ExceptionHandler(ApplicationInstanceCreationException.class)
    public ModelAndView instantiationError(ApplicationInstanceCreationException e) {
        Map<String, Object> model = new HashMap<>();
        model.put("appname", e.getRequested().getName());
        model.put("appid", e.getApplicationId());
        model.put("errortype", e.getType().toString());

        model.put("navigation", myNavigationService.getNavigation(null));
        model.put("currentLanguage", currentLanguage());
        model.put("user", user());

        return new ModelAndView("store/instantiation-error", model);
    }


}

