package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/popup")
public class PopupController {

    private final UserService userService;

    @Autowired
    public PopupController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/**")
    public String show() {
        if (userService.requiresLogout()) {
            return "redirect:/logout";
        }
        return "forward:/index.html";
    }
}
