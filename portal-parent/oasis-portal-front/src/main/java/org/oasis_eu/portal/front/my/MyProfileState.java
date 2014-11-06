package org.oasis_eu.portal.front.my;

import org.oasis_eu.portal.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(MyProfileState.class);

	private Map<String, FormLayout> layouts;
	
	@Value("${kernel.auth.password_change_endpoint:''}")
    protected String passwordChangeEndpoint;
    
    @PostConstruct
    public void reset() {
    	
    	// Note: the widget IDs must match the OpenID properties.
    	// The values are bound in the method below.
    	
    	layouts = new HashMap<String, FormLayout>(); 
    	
    	FormLayout accountFormLayout = new FormLayout(LAYOUT_ACCOUNT, "my.profile.title.account", LAYOUT_FORM_ACTION, LAYOUT_FORM_CLASS);
    	accountFormLayout.setOrder(1);
    	accountFormLayout.appendWidget(new AvatarWidget("pictureUrl", "my.profile.account.avatar", getAvailableAvatars()));
    	accountFormLayout.appendWidget(new FormWidgetText("email",
        		"my.profile.account.email"));
    	accountFormLayout.appendWidget(new FormWidgetUrlButton("password",
        		"my.profile.account.password", "my.profile.account.changepassword", passwordChangeEndpoint));
    	accountFormLayout.appendWidget(new FormWidgetDropdown("locale",
        		"my.profile.account.language"));
    	layouts.put(accountFormLayout.getId(), accountFormLayout);
        
        FormLayout idFormLayout = new FormLayout(LAYOUT_IDENTITY, "my.profile.personal.identity", LAYOUT_FORM_ACTION, LAYOUT_FORM_CLASS);
        idFormLayout.setOrder(2);
        idFormLayout.appendWidget(new FormWidgetText("givenName",
        		"my.profile.personal.firstname"));
        idFormLayout.appendWidget(new FormWidgetText("familyName",
        		"my.profile.personal.lastname"));
        idFormLayout.appendWidget(new FormWidgetDate("birthdate",
        		"my.profile.personal.birthdate"));
        idFormLayout.appendWidget(new FormWidgetDropdown("gender",
        		"my.profile.personal.gender")
    		.addOption("female", "my.profile.personal.gender.female")
        	.addOption("male", "my.profile.personal.gender.male"));
        idFormLayout.appendWidget(new FormWidgetText("phoneNumber",
        		"my.profile.personal.phonenumber"));
        layouts.put(idFormLayout.getId(), idFormLayout);
        
        FormLayout adFormLayout = new FormLayout(LAYOUT_ADDRESS, "my.profile.personal.address", LAYOUT_FORM_ACTION, LAYOUT_FORM_CLASS);
        idFormLayout.setOrder(3);
        adFormLayout.appendWidget(new FormWidgetText("address.streetAddress",
        		"my.profile.personal.streetaddress"));
        adFormLayout.appendWidget(new FormWidgetText("address.locality",
        		"my.profile.personal.locality"));
        adFormLayout.appendWidget(new FormWidgetText("address.postalCode",
        		"my.profile.personal.postalcode"));
        adFormLayout.appendWidget(new FormWidgetText("address.country",
        		"my.profile.personal.country"));
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
	
	private List<String> getAvailableAvatars() {
		// TODO Where/how do we store avatars?
		return Arrays.asList("/img/my/avatar/img-19.png",
				"/img/my/avatar/img-20.png", "/img/my/avatar/img-21.png",
				"/img/my/avatar/img-22.png", "/img/my/avatar/img-23.png");
	}
    
}
