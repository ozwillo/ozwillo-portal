package org.oasis_eu.portal.front.generic;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.portal.services.UserInfoService;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * User: schambon Date: 6/11/14
 */
abstract public class PortalController {

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private HttpServletRequest request;

	@ModelAttribute("languages")
	public Languages[] languages() {
		return Languages.values();
	}

	@ModelAttribute("currentLanguage")
	public Languages currentLanguage() {
		Locale currentLocale = RequestContextUtils.getLocale(request);
		return Languages.getByLocale(currentLocale, Languages.ENGLISH);
	}

	@ModelAttribute("user")
	public UserInfo user() {
		return userInfoService.currentUser();
	}

}
