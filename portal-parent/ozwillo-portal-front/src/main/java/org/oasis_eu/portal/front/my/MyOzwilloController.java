package org.oasis_eu.portal.front.my;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.front.generic.i18nMessages;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.portal.services.PortalDashboardService;
import org.oasis_eu.portal.services.PortalNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Controller
@RequestMapping("/my")
public class MyOzwilloController extends PortalController {

	//private static final Logger logger = LoggerFactory.getLogger(MyOzwilloController.class);

	@Autowired
	private PortalDashboardService portalDashboardService;

	@Autowired
	private PortalNotificationService notificationService;

	@Autowired
	private MyNavigationService myNavigationService;

	@Autowired
	private MessageSource messageSource;

	@ModelAttribute("i18n")
	public Map<String, String> getI18n(HttpServletRequest request) throws JsonProcessingException {
		Locale locale = RequestContextUtils.getLocale(request);

		Map<String, String> i18n = new HashMap<>();
		i18n.putAll(i18nMessages.getI18n_i18keys(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_generickeys(locale, messageSource));
		i18n.putAll(i18nMessages.getI18nContactKeys(locale, messageSource));

		return i18n;
	}


	@ModelAttribute("notif_i18n")
	public Map<String, String> getNotifI18n(HttpServletRequest request) throws JsonProcessingException {
		Locale locale = RequestContextUtils.getLocale(request);

		Map<String, String> i18n = new HashMap<>();

		List<String> keys = Arrays.asList("ui.notifications", "notif.date", "notif.app", "notif.message", "notif.archive", "notif.manage", "notif.no-notification", "notif.unread", "notif.read", "notif.any", "notif.all-apps");
		i18n.putAll(keys.stream().collect(Collectors.toMap(k -> k, k -> messageSource.getMessage(k, new Object[0], locale))));
		i18n.putAll(i18nMessages.getI18nContactKeys(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_generickeys(locale, messageSource));

		return i18n;
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
