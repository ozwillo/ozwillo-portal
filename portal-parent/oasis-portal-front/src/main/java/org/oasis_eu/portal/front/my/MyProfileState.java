package org.oasis_eu.portal.front.my;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.oasis_eu.portal.core.controller.Languages;
import org.oasis_eu.portal.model.AvatarWidget;
import org.oasis_eu.portal.model.FormLayout;
import org.oasis_eu.portal.model.FormWidget;
import org.oasis_eu.portal.model.FormWidgetDate;
import org.oasis_eu.portal.model.FormWidgetDropdown;
import org.oasis_eu.portal.model.FormWidgetHidden;
import org.oasis_eu.portal.model.FormWidgetText;
import org.oasis_eu.portal.model.FormWidgetUrlButton;
import org.oasis_eu.portal.services.NameDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;

/**
 * 
 * @author mkalamalami
 *
 */
@Controller
//@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyProfileState {


	public static final String LAYOUT_ACCOUNT = "account";
	
	public static final String LAYOUT_IDENTITY = "identity";

	public static final String LAYOUT_ADDRESS = "address";

	public static final String LAYOUT_FORM_ACTION = "/my/profile/save";

	public static final String LAYOUT_FORM_CLASS = "personal-data-form";

	private static final Logger logger = LoggerFactory.getLogger(MyProfileState.class);

	private Map<String, FormLayout> layouts;
	
	@Value("${kernel.auth.password_change_endpoint:''}")
	protected String passwordChangeEndpoint;

	@Value("${web.avatarPath:}")
	private String avatarPath;
	
	private List<String> availableAvatars;
	
	/** to walk avatar files */
	@Autowired
	private ApplicationContext applicationContext;
	
	/** to best display an unknown locale */
	@Autowired
	private NameDefaults nameDefaults;

	@PostConstruct
	public void reset() {

		// loading available avatars :
		if (!avatarPath.startsWith("/")) {
			avatarPath = "/" + avatarPath;
		}
		if (!avatarPath.endsWith("/")) {
			avatarPath = avatarPath + "/";
		}
		try {
			String avatarPattern = "classpath:public" + avatarPath + "*";
			Resource[] avatarResources = applicationContext.getResources(avatarPattern);
			availableAvatars = Arrays.asList(avatarResources).stream()
					.filter(avatarResource -> avatarResource.exists()) // else happens ? LATER check file extension of p.getFileName().toString()
					.map(avatarResource -> {
						try {
							String urlString = avatarResource.getURL().toString();
							return urlString.substring(urlString.indexOf(avatarPath));
						} catch (IOException ioex) {
							logger.error("Error loading avatar resource " + avatarResource, ioex);
							return null;
						} // finally { stream.close(); // ideally...
					})
					.filter(avatarResource -> avatarResource != null) // in case IOException above
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e); // don't start if fails
		} // finally { stream.close(); // ideally...


		// Note: the widget IDs must match the OpenID properties.
		// The values are bound in the method below.

		layouts = new HashMap<>();

		FormLayout accountFormLayout = new FormLayout(LAYOUT_ACCOUNT, "my.profile.title.account", LAYOUT_FORM_ACTION, LAYOUT_FORM_CLASS);
		accountFormLayout.setOrder(1);
		accountFormLayout.appendWidget(new FormWidgetText("nickname", "my.profile.personal.nickname"));
		accountFormLayout.appendWidget(new AvatarWidget("pictureUrl", "my.profile.account.avatar", getAvailableAvatars()));
		accountFormLayout.appendWidget(new FormWidgetText("email", "my.profile.account.email"));
		accountFormLayout.appendWidget(new FormWidgetUrlButton("password", "my.profile.account.password", "my.profile.account.changepassword", passwordChangeEndpoint));
		accountFormLayout.appendWidget(new FormWidgetDropdown("locale", "my.profile.account.language", uiLocales -> {
					Languages keyLanguages = nameDefaults.getBestLanguage(uiLocales); // including "en-GB fr" http://docs.oracle.com/javase/tutorial/i18n/locale/create.html
					return (keyLanguages != null) ? keyLanguages.getLanguage() : null;
				}));
		layouts.put(accountFormLayout.getId(), accountFormLayout);

		FormLayout idFormLayout = new FormLayout(LAYOUT_IDENTITY, "my.profile.personal.identity", LAYOUT_FORM_ACTION, LAYOUT_FORM_CLASS);
		idFormLayout.setOrder(2);
		idFormLayout.appendWidget(new FormWidgetText("givenName", "my.profile.personal.firstname"));
		idFormLayout.appendWidget(new FormWidgetText("familyName", "my.profile.personal.lastname"));
		idFormLayout.appendWidget(new FormWidgetDate("birthdate", "my.profile.personal.birthdate"));
		idFormLayout.appendWidget(new FormWidgetDropdown("gender","my.profile.personal.gender")
			.addOption("female", "my.profile.personal.gender.female")
			.addOption("male", "my.profile.personal.gender.male"));
		idFormLayout.appendWidget(new FormWidgetText("phoneNumber", "my.profile.personal.phonenumber"));
		layouts.put(idFormLayout.getId(), idFormLayout);

		FormLayout adFormLayout = new FormLayout(LAYOUT_ADDRESS, "my.profile.personal.address", LAYOUT_FORM_ACTION, LAYOUT_FORM_CLASS);
		idFormLayout.setOrder(3);
		adFormLayout.appendWidget(new FormWidgetHidden("address.country","my.profile.personal.country"));
		adFormLayout.appendWidget(new FormWidgetHidden("address.locality", "my.profile.personal.locality"));
		adFormLayout.appendWidget(new FormWidgetText("address.postalCode", "my.profile.personal.postalcode"));
		adFormLayout.appendWidget(new FormWidgetText("address.streetAddress","my.profile.personal.streetaddress"));
		layouts.put(adFormLayout.getId(), adFormLayout);
	}
	
	public List<FormLayout> getLayouts() {
		return layouts.values().stream().sorted().collect(Collectors.toList());
	}
	
	public FormLayout getLayout(String layoutId) {
		return layouts.get(layoutId);
	}
	
	
	public String getWidgetValue(String key) {
		for (FormLayout layout : layouts.values()) {
			FormWidget widget = layout.getWidget(key);
			if (widget != null) {
				return widget.getValue();
			}
		}
		return null;
	}
	
	/**
	 * Returns avatar images below the img/my/avatar tree
	 * (uploaded avatars are stored in mongo elsewhere) 
	 * @return
	 */
	private List<String> getAvailableAvatars() {
		return availableAvatars;
	}

}
