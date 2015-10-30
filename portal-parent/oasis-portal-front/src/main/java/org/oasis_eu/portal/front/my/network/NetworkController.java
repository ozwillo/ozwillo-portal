package org.oasis_eu.portal.front.my.network;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
 * @author schambon
 * @author mkalamalami
 */
@Controller
@RequestMapping("/my/network")
public class NetworkController extends PortalController {

	//private static final Logger logger = LoggerFactory.getLogger(NetworkController.class);

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
		i18n.putAll(i18nMessages.getI18n_i18keys(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_generickeys(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_searchOrganization(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_createOrModifyOrganization(locale, messageSource));
		i18n.putAll(i18nMessages.getI18n_errors(locale, messageSource));

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