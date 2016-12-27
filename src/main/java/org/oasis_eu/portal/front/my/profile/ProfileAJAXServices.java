package org.oasis_eu.portal.front.my.profile;

import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.portal.model.OasisLocales;
import org.oasis_eu.portal.model.kernel.UserProfile;
import org.oasis_eu.portal.model.profile.UIUserProfile;
import org.oasis_eu.portal.services.kernel.UserProfileService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/my/api/profile")
public class ProfileAJAXServices extends BaseAJAXServices {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserProfileService userProfileService;

    @Value("${kernel.auth.password_change_endpoint:''}")
    private String passwordChangeEndpoint;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public UIUserProfile userInfos() {
        UserProfile userProfile = userProfileService.findUserProfile(userInfoService.currentUser().getUserId());
        List<String> languages = OasisLocales.locales().stream().map(Locale::getLanguage).collect(Collectors.toList());
        return new UIUserProfile(userProfile, languages, passwordChangeEndpoint);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void save(@RequestBody UserProfile userProfile) {
        // TODO : why ??
        userProfile.setName(userProfile.getNickname()); // force name = nickname
        userProfileService.saveUserProfile(userProfile);
    }
}
