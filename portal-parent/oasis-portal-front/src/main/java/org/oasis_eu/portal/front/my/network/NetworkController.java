package org.oasis_eu.portal.front.my.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author schambon
 * @author mkalamalami
 */
@Controller
@RequestMapping("/my/network")
public class NetworkController extends PortalController {

    private static final Logger logger = LoggerFactory.getLogger(NetworkController.class);


    private static List<String> i18keys = Arrays.asList("find-or-create-organization", "information",
            "leave", "invite", "admin", "user", "email", "yes-i-want-to-leave", "confirm-leave",
            "organization-type.PUBLIC_BODY", "organization-type.COMPANY", "organization-name", "organization-type", "create",
            "by", "will-be-deleted", "confirm-trash.title", "confirm-trash.body", "confirm-untrash.title", "confirm-untrash.body");

    private static List<String> generickeys = Arrays.asList("save", "cancel", "close", "confirm", "delete",
            "loading", "go", "general-error", "edit", "remove", "location", "unexpected_error");

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MyNavigationService myNavigationService;


    @ModelAttribute("navigation")
    private List<MyNavigation> getNavigation() {
        return myNavigationService.getNavigation("network");
    }

    @ModelAttribute("i18n")
    public Map<String, String> getI18n(HttpServletRequest request) throws JsonProcessingException {
        Locale locale = RequestContextUtils.getLocale(request);

        Map<String, String> i18n = new HashMap<>();
        i18n.putAll(i18keys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage("my.network." + k, new Object[]{}, locale))));
        i18n.putAll(generickeys.stream().collect(Collectors.toMap(k -> "ui." + k, k -> messageSource.getMessage("ui." + k, new Object[]{}, locale))));
        return i18n;
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public String network() throws ExecutionException {
        if (requiresLogout()) {
            return "redirect:/logout";
        }
        return "network/my-network";
    }


}