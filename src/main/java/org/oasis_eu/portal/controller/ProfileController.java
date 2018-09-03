package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.model.OasisLocales;
import org.oasis_eu.portal.model.kernel.user.UserProfile;
import org.oasis_eu.portal.model.user.UIUserProfile;
import org.oasis_eu.portal.services.kernel.FranceConnectService;
import org.oasis_eu.portal.services.kernel.UserProfileService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/my/api/profile")
public class ProfileController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private FranceConnectService franceConnectService;

    @Value("${kernel.auth.password_change_endpoint:''}")
    private String passwordChangeEndpoint;

    @Value("${kernel.france_connect.link_endpoint:''}")
    private String linkFranceConnectEndpoint;

    @Value("${kernel.france_connect.unlink_endpoint:''}")
    private String unlinkFranceConnectEndpoint;

    @Value("${kernel.france_connect.enabled:''}")
    private boolean franceConnectEnabled;

    @GetMapping
    public UIUserProfile userInfos() {
        UserProfile userProfile = userProfileService.findUserProfile(userInfoService.currentUser().getUserId());
        List<String> languages = OasisLocales.locales().stream().map(Locale::getLanguage).collect(Collectors.toList());
        return new UIUserProfile(userProfile, languages, passwordChangeEndpoint,
                linkFranceConnectEndpoint, unlinkFranceConnectEndpoint, franceConnectEnabled);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void save(@RequestBody UserProfile userProfile) {
        // TODO : why ??
        userProfile.setName(userProfile.getNickname()); // force name = nickname
        userProfileService.saveUserProfile(userProfile);
    }

    @GetMapping("/franceconnect")
    public UIUserProfile userFranceConnectInfo() {
        UserProfile franceConnectProfile = franceConnectService.getFranceConnectInfo();
        UserProfile userProfile = userProfileService.findUserProfile(userInfoService.currentUser().getUserId());
        List<String> languages = OasisLocales.locales().stream().map(Locale::getLanguage).collect(Collectors.toList());
        return new UIUserProfile(userProfile, franceConnectProfile, languages);
    }
}
