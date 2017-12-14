package org.oasis_eu.portal.front.my;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.front.generic.i18nMessages;
import org.oasis_eu.portal.services.MyNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Controller
@RequestMapping("/my")
public class MyOzwilloController extends PortalController {

    //private static final Logger logger = LoggerFactory.getLogger(MyOzwilloController.class);

    /*@ModelAttribute("model")
    public Map<String, String> getNotifI18n(HttpServletRequest request) throws JsonProcessingException {
        Locale locale = RequestContextUtils.getLocale(request);

        Map<String, String> i18n = new HashMap<>();

        List<String> keys = Arrays.asList("ui.notifications", "notif.date", "notif.app", "notif.message", "notif.archive", "notif.manage", "notif.no-notification", "notif.unread", "notif.read", "notif.any", "notif.all-apps");
        i18n.putAll(keys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage(k, new Object[0], locale))));
        i18n.putAll(i18nMessages.getI18nContactKeys(locale, messageSource));
        i18n.putAll(i18nMessages.getI18n_generickeys(locale, messageSource));

        return i18n;
    }*/

    /*@RequestMapping(method = RequestMethod.GET, value = "/notif")
    public String notifications(Model model, HttpServletRequest request) {
        model.addAttribute("navigation", myNavigationService.getNavigation("notifications"));
        return "my-notif";
    }*/

    @RequestMapping(method = RequestMethod.GET, value = "*")
    public String show() {
        if (requiresLogout()) {
            return "redirect:/logout";
        }
        return "/index";
    }

}
