package org.oasis_eu.portal.front.my;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.FormLayout;
import org.oasis_eu.portal.model.FormLayoutMode;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.spring.kernel.model.Address;
import org.oasis_eu.spring.kernel.model.UserAccount;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.service.UserAccountService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	
	@Autowired
	private UserAccountService userAccountService;
	
	@ModelAttribute("currentUser")
	UserAccount getCurrentUserAccount() {
		
		return new UserAccount(userInfoService.currentUser());
	}
	
	@InitBinder
	protected void initBinder(WebDataBinder binder){
		
		binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
			
		    public void setAsText(String value) {
		        try {
		        	//DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withLocale(new Locale(currentLanguage().getLanguage())); // Languages.locale renvoie en pour locale en-GB
		            //setValue(LocalDate.parse(value, dateTimeFormatter));
		        	setValue(LocalDate.parse(value));
		        } catch(DateTimeParseException e) {
		        	
		            setValue(null);
		        }
		    }

		    public String getAsText() {
		        return ((LocalDate) getValue()).toString();
		    }        

		});
	}

	@RequestMapping(method = RequestMethod.GET, value = "")
	public String profile(@ModelAttribute("currentUser") UserAccount currentUser, Model model) {
		initProfileModel(model);
		// myProfileState.reset(); // reset still called (PostConstruct annotation)
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
			@ModelAttribute("currentUser") @Valid UserAccount currentUser, BindingResult result, Model model) {

		if(result.hasErrors()) {
			
			//return "my-profile";
			return "includes/my-profile-fragments :: layout";
		}
		userAccountService.saveUserAccount(currentUser);

		myProfileState.getLayout(layoutId).setMode(FormLayoutMode.VIEW);
		myProfileState.refreshLayoutValues();
		return "redirect:/my/profile/fragment/layout/" + layoutId;
	}

	// we should use currentUser modelAtribute as with saveLayout to benefit from automatic mapping
	// and validation, however it would prevent from modifying language and email until
	// general info are submitted (validation would fail on required firstname, family name, etc. fields).
	@RequestMapping(method = RequestMethod.POST, value = "/save/language")
	public String saveLanguage(@RequestParam("locale") String locale,
			Model model) {
		if (Languages.getByLocale(Locale.forLanguageTag(locale)) != null) {
			saveSingleUserInfo("locale", locale);
		}
		initProfileModel(model);
		return "my-profile";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/save/email")
	public String saveEmail(@RequestParam("email") String email, BindingResult result, Model model) {
		// Source: http://www.regular-expressions.info/email.html
		// ("almost RFC 5322")
		if (email
				.matches("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")) {
			saveSingleUserInfo("email", email);
		} else {
			
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

	private UserAccount buildUserAccount(MultiValueMap<String, String> data) {
		
		UserAccount userAccount = new UserAccount(userInfoService.currentUser());
		
		if(!StringUtils.isEmpty(data.getFirst("email"))) {
		
			userAccount.setEmail(data.getFirst("email"));
			userInfoService.currentUser().setEmailVerified(false);
		}
		
		if(!StringUtils.isEmpty(data.getFirst("locale"))) {
			
			userAccount.setLocale(data.getFirst("locale"));
		}
		
		if(!StringUtils.isEmpty(data.getFirst("picture"))) {
			
			userAccount.setPictureUrl(data.getFirst("picture"));
		}
		
		if(!StringUtils.isEmpty(data.getFirst("given_name"))) {
			
			userAccount.setGivenName(data.getFirst("given_name"));
		}
		
		if(!StringUtils.isEmpty(data.getFirst("family_name"))) {
			
			userAccount.setFamilyName(data.getFirst("family_name"));
		}
    	
		String birthdate = data.getFirst("birthdate");
		if (!StringUtils.isEmpty(birthdate)) {
			userAccount.setBirthdate(LocalDate.parse(birthdate));
		}
		
		if(!StringUtils.isEmpty(data.getFirst("gender"))) {
			
			userAccount.setGender(data.getFirst("gender"));
		}

		if(!StringUtils.isEmpty(data.getFirst("phone_number"))) {
			
			userAccount.setPhoneNumber(data.getFirst("phone_number"));
		}
		
		if(!StringUtils.isEmpty(data.getFirst("street_address"))) {
			
			userAccount.getAddress().setStreetAddress(data.getFirst("street_address"));
		}
		
		if(!StringUtils.isEmpty(data.getFirst("locality"))) {
			
			userAccount.getAddress().setLocality(data.getFirst("locality"));
		}
		
		if(!StringUtils.isEmpty(data.getFirst("postal_code"))) {
			
			userAccount.getAddress().setPostalCode(data.getFirst("postal_code"));
		}
	
		if(!StringUtils.isEmpty(data.getFirst("country"))) {
			
			userAccount.getAddress().setCountry(data.getFirst("country"));
		}
		
		return userAccount;
	}

	protected void saveSingleUserInfo(String key, String value) {
		MultiValueMap<String, String> userData = new LinkedMultiValueMap<String, String>();
		userData.put(key, Arrays.asList(value));
		userAccountService.saveUserAccount(buildUserAccount(userData));
		myProfileState.refreshLayoutValues();
	}

	protected void initProfileModel(Model model) {
		model.addAttribute("navigation",
				myNavigationService.getNavigation("profile"));
		model.addAttribute("layouts", myProfileState.getLayouts());
		model.addAttribute("availableAvatars", getAvailableAvatars());
    if (user().getLocale() != null) {
		    model.addAttribute("userLanguage", Languages.getByLocale(Locale.forLanguageTag(
				    user().getLocale()), Languages.ENGLISH));
    } else {
        model.addAttribute("userLanguage", Languages.ENGLISH);
    }
	}

	private List<String> getAvailableAvatars() {
		// TODO Where/how do we store avatars?
		return Arrays.asList("/img/my/avatar/img-19.png",
				"/img/my/avatar/img-20.png", "/img/my/avatar/img-21.png",
				"/img/my/avatar/img-22.png", "/img/my/avatar/img-23.png");
	}

};
