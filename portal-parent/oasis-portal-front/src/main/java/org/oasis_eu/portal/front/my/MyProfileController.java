package org.oasis_eu.portal.front.my;

import java.util.stream.Collectors;

import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.FormLayout;
import org.oasis_eu.portal.model.FormLayoutMode;
import org.oasis_eu.portal.services.MyNavigationService;
import org.oasis_eu.portal.services.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
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
@RequestMapping("/my")
public class MyProfileController extends PortalController {

    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(MyProfileController.class);

    @Autowired
    private MyNavigationService myNavigationService;

    @Autowired(required = false)
    private MyProfileState myProfileState;

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping(method = RequestMethod.GET, value="/profile")
    public String profile(Model model) {
    	initProfileModel(model);
        return "my-profile";
    }

    @RequestMapping(method = RequestMethod.GET, value="/profile/layout/{id}")
    public String getProfileLayout(@PathVariable("id") String layoutId, Model model) {
    	initProfileModel(model);
    	model.addAttribute("layout", myProfileState.getLayout(layoutId));
    	return "includes/my-profile-fragments :: layout";
    }

    @RequestMapping(method = RequestMethod.POST, value="/profile/mode")
    public String toggleProfileLayout(@RequestParam("mode") String mode, @RequestParam("id") String layoutId, Model model) {
    	FormLayout formLayout = myProfileState.getLayout(layoutId);
    	if (formLayout != null) {
    		formLayout.setMode(FormLayoutMode.valueOf(mode));
    	}
    	initProfileModel(model);
    	model.addAttribute("layout", formLayout);
    	
    	return "redirect:/my/profile/layout/" + layoutId;
    }
    
    @RequestMapping(method = RequestMethod.POST, value="/profile/save")
    public String saveLayout(@RequestBody MultiValueMap<String, String> data, Model model) {
    	
    	userInfoService.saveCurrentUser(
    			data.entrySet().stream()
    				.filter(entry -> !"layout-id".equals(entry.getKey()))
    				.collect(Collectors.toMap(
    						entry -> entry.getKey(),
    						entry -> entry.getValue().get(0))));
    	
    	String layoutId = data.get("layout-id").get(0);
    	myProfileState.getLayout(layoutId).setMode(FormLayoutMode.VIEW);
    	myProfileState.refreshLayoutValues();
    	return "redirect:/my/profile/layout/" + layoutId;
    }
    
    protected void initProfileModel(Model model) {
        model.addAttribute("navigation", myNavigationService.getNavigation("profile"));
        model.addAttribute("layouts", myProfileState.getLayouts());
    }

}
;