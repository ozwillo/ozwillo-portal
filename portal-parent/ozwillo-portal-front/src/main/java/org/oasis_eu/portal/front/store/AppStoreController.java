package org.oasis_eu.portal.front.store;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.oasis_eu.portal.config.AppStoreNavigationStatus;
import org.oasis_eu.portal.core.model.appstore.ApplicationInstanceCreationException;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.front.generic.i18nMessages;
import org.oasis_eu.portal.model.MyNavigation;
import org.oasis_eu.portal.services.MyNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

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

	@ModelAttribute("i18n")
	public Map<String, String> i18n(HttpServletRequest request) throws JsonProcessingException {
		Locale locale = RequestContextUtils.getLocale(request);
		Map<String, String> i18n = new HashMap<>();

		i18n.putAll(i18nMessages.getI18n_networkkeys(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_languagekeys(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_storekeys(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_generickeys(locale, messageSource));
		i18n.putAll(i18nMessages.getI18nContactKeys(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_searchOrganization(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_createOrModifyOrganization(locale, messageSource));

		return i18n;
	}


	@RequestMapping(method = RequestMethod.GET, value = {"", "/"})
	public String main(@PathVariable String lang, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
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

