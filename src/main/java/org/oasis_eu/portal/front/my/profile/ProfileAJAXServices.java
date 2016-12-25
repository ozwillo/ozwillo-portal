package org.oasis_eu.portal.front.my.profile;

import org.oasis_eu.portal.front.generic.BaseAJAXServices;
import org.oasis_eu.spring.kernel.model.UserAccount;
import org.oasis_eu.spring.kernel.service.UserAccountService;
import org.oasis_eu.spring.kernel.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/my/api/profile")
public class ProfileAJAXServices extends BaseAJAXServices {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserAccountService userAccountService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public UserAccount currentUser() {
        return new UserAccount(userInfoService.currentUser());
    }

    @RequestMapping(method = RequestMethod.POST)
    public void save(@RequestBody UserAccount userAccount) {
        userAccount.setName(userAccount.getNickname()); // force name = nickname
        userAccountService.saveUserAccount(userAccount);
    }
}
