package org.oasis_eu.portal.front.my;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.FormLayout;
import org.oasis_eu.portal.model.FormLayoutMode;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.spring.kernel.model.Address;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 
 * @author mkalamalami
 *
 */
@Controller
@RequestMapping("/my/profile")
public class MyProfileController extends PortalController {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(MyProfileController.class);

	@Autowired
	private MyNavigationService myNavigationService;

	@Autowired(required = false)
	private MyProfileState myProfileState;

	@Autowired
	private UserInfoService userInfoService;

	@RequestMapping(method = RequestMethod.GET, value = "")
	public String profile(Model model) {
		initProfileModel(model);
		// myProfileState.reset();
		return "my-profile";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/fragment/account-data")
	public String profileAccountDataFragment(Model model) {
		initProfileModel(model);
		return "my-profile :: account-data";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/fragment/layout/{id}")
	public String profileLayoutFragment(@PathVariable("id") String layoutId,
			Model model) {
		initProfileModel(model);
		model.addAttribute("layout", myProfileState.getLayout(layoutId));
		return "includes/my-profile-fragments :: layout";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/mode")
	public String toggleProfileLayout(@RequestParam("mode") String mode,
			@RequestParam("id") String layoutId, Model model) {
		FormLayout formLayout = myProfileState.getLayout(layoutId);
		if (formLayout != null) {
			formLayout.setMode(FormLayoutMode.valueOf(mode));
		}
		initProfileModel(model);
		model.addAttribute("layout", formLayout);

		return "redirect:/my/profile/fragment/layout/" + layoutId;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/save/{layoutId}")
	public String saveLayout(@PathVariable("layoutId") String layoutId,
			@RequestBody MultiValueMap<String, String> data, Model model) {

		UserInfo userInfo = buildUserInfo(data);

		userInfoService.saveUserInfo(userInfo);

		myProfileState.getLayout(layoutId).setMode(FormLayoutMode.VIEW);
		myProfileState.refreshLayoutValues();
		return "redirect:/my/profile/fragment/layout/" + layoutId;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/save/language")
	public String saveLanguage(@RequestParam("locale") String locale,
			Model model) {
		if (Languages.getByLocale(new Locale(locale)) != null) {
			saveSingleUserInfo("locale", locale);
		}
		initProfileModel(model);
		return "my-profile";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/save/email")
	public String saveEmail(@RequestParam("email") String email, Model model) {
		// Source: http://www.regular-expressions.info/email.html
		// ("almost RFC 5322")
		if (email
				.matches("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")) {
			saveSingleUserInfo("email", email);
		}
		return "redirect:/my/profile/fragment/account-data";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/save/avatar")
	public String saveAvatar(@RequestParam("avatar") String avatar, Model model) {
		if (getAvailableAvatars().contains(avatar)) {
			saveSingleUserInfo("picture", avatar);
		}
		return "redirect:/my/profile/fragment/account-data";
	}

	private UserInfo buildUserInfo(MultiValueMap<String, String> data) {
		UserInfo userInfo = new UserInfo();
		userInfo.setGivenName(data.getFirst("given_name"));
		userInfo.setFamilyName(data.getFirst("family_name"));
		String birthdate = data.getFirst("birthdate");
		if (!StringUtils.isEmpty(birthdate)) {
			userInfo.setBirthdate(LocalDate.parse(birthdate));
		}
		userInfo.setGender(data.getFirst("gender"));
		userInfo.setPhoneNumber(data.getFirst("phone_number"));
	
		Address address = new Address();
		address.setStreetAddress(data.getFirst("street_address"));
		address.setLocality(data.getFirst("locality"));
		address.setPostalCode(data.getFirst("postal_code"));
		address.setCountry(data.getFirst("country"));
		userInfo.setAddress(address);
		return userInfo;
	}

	protected void saveSingleUserInfo(String key, String value) {
		MultiValueMap<String, String> userData = new LinkedMultiValueMap<String, String>();
		userData.put(key, Arrays.asList(value));
		userInfoService.saveUserInfo(buildUserInfo(userData));
		myProfileState.refreshLayoutValues();
	}

	protected void initProfileModel(Model model) {
		model.addAttribute("navigation",
				myNavigationService.getNavigation("profile"));
		model.addAttribute("layouts", myProfileState.getLayouts());
		model.addAttribute("availableAvatars", getAvailableAvatars());
		model.addAttribute("userLanguage", Languages.getByLocale(new Locale(
				user().getLocale()), Languages.ENGLISH));
	}

	private List<String> getAvailableAvatars() {
		// TODO Where/how do we store avatars?
		return Arrays.asList("/img/my/avatar/img-19.png",
				"/img/my/avatar/img-20.png", "/img/my/avatar/img-21.png",
				"/img/my/avatar/img-22.png", "/img/my/avatar/img-23.png");
	}

};
