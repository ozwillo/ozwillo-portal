package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/my")
public class MyOzwilloController {

    private final UserService userService;

    @Autowired
    public MyOzwilloController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String show() {
        if (userService.requiresLogout()) {
            return "redirect:/logout";
        }
        return "forward:/index.html";
    }
}
