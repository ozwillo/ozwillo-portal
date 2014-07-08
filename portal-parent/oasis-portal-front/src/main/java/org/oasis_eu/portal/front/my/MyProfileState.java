package org.oasis_eu.portal.front.my;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.oasis_eu.portal.model.FormLayout;
import org.oasis_eu.portal.model.FormWidgetDate;
import org.oasis_eu.portal.model.FormWidgetDropdown;
import org.oasis_eu.portal.model.FormWidgetText;
import org.oasis_eu.portal.services.UserInfoService;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;

/**
 * 
 * @author mkalamalami
 *
 */
@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyProfileState {

    public static final String LAYOUT_IDENTITY = "identity";

    public static final String LAYOUT_ADDRESS = "address";
    
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(MyProfileState.class);

    @Autowired
    private UserInfoService userInfoHelper;
    
	private Map<String, FormLayout> layouts;
    
    @PostConstruct
    public void reset() {
    	
    	// Note: the widget IDs must match the OpenID properties.
    	// The values are bound in the method below.
    	
    	layouts = new HashMap<String, FormLayout>(); 
    	
        FormLayout idFormLayout = new FormLayout(LAYOUT_IDENTITY, "my.profile.personal.identity");
        idFormLayout.setOrder(1);
        idFormLayout.appendWidget(new FormWidgetText("given_name",
        		"my.profile.personal.firstname"));
        idFormLayout.appendWidget(new FormWidgetText("family_name",
        		"my.profile.personal.lastname"));
        idFormLayout.appendWidget(new FormWidgetDate("birthdate",
        		"my.profile.personal.birthdate"));
        idFormLayout.appendWidget(new FormWidgetDropdown("gender",
        		"my.profile.personal.gender")
    		.addOption("female", "my.profile.personal.gender.female")
        	.addOption("male", "my.profile.personal.gender.male"));
        idFormLayout.appendWidget(new FormWidgetText("phone_number",
        		"my.profile.personal.phonenumber"));
        layouts.put(idFormLayout.getId(), idFormLayout);
        
        FormLayout adFormLayout = new FormLayout(LAYOUT_ADDRESS, "my.profile.personal.address");
        idFormLayout.setOrder(2);
        adFormLayout.appendWidget(new FormWidgetText("street_address",
        		"my.profile.personal.streetaddress"));
        adFormLayout.appendWidget(new FormWidgetText("locality",
        		"my.profile.personal.locality"));
        adFormLayout.appendWidget(new FormWidgetText("postal_code",
        		"my.profile.personal.postalcode"));
        adFormLayout.appendWidget(new FormWidgetText("country",
        		"my.profile.personal.country"));
        layouts.put(adFormLayout.getId(), adFormLayout);
        
        refreshLayoutValues();
    }

	public void refreshLayoutValues() {
    	UserInfo userInfo = userInfoHelper.currentUser();
		
    	FormLayout idFormLayout = layouts.get(LAYOUT_IDENTITY);
    	idFormLayout.getWidget("given_name").setValue(userInfo.getGivenName());
    	idFormLayout.getWidget("family_name").setValue(userInfo.getFamilyName());
    	idFormLayout.getWidget("birthdate").setValue(userInfo.getBirthdate().toString());
    	idFormLayout.getWidget("gender").setValue(userInfo.getGender());
    	idFormLayout.getWidget("phone_number").setValue(userInfo.getPhoneNumber());

    	FormLayout adFormLayout = layouts.get(LAYOUT_ADDRESS);
    	adFormLayout.getWidget("street_address").setValue(userInfo.getStreetAddress());
    	adFormLayout.getWidget("locality").setValue(userInfo.getLocality());
    	adFormLayout.getWidget("postal_code").setValue(userInfo.getPostalCode());
    	adFormLayout.getWidget("country").setValue(userInfo.getCountry());
	}
	
	public List<FormLayout> getLayouts() {
		return layouts.values().stream().sorted().collect(Collectors.toList());
	}
	
	public FormLayout getLayout(String layoutId) {
		return layouts.get(layoutId);
	}
    
}
