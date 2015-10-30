package org.oasis_eu.portal.front.my.appsmanagement;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.front.generic.i18nMessages;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * User: schambon
 * Date: 7/29/14
 */

@Controller
@RequestMapping("/my/appsmanagement")
public class MyAppsManagementController extends PortalController {

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
		i18n.putAll(i18nMessages.getI18n_myApps(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_generickeys(locale, messageSource));

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
