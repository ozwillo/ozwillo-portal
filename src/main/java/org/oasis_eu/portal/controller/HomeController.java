package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserService userService;

    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/my";
    }

    @GetMapping("/my")
    public String show() {
        if (userService.requiresLogout()) {
            return "redirect:/logout";
        }
        return "forward:/index.html";
    }
}
