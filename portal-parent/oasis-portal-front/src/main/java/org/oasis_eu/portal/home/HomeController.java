package org.oasis_eu.portal.home;

import org.oasis_eu.portal.generic.PortalController;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * User: schambon
 * Date: 5/13/14
 */
@Controller
public class HomeController extends PortalController {

    @ModelAttribute("username")
    public String username() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication instanceof OpenIdCAuthentication) {
            return ((OpenIdCAuthentication) authentication).getUserInfo().getName();
        } else return null;
    }

    @ModelAttribute("hostname")
    public String hostname() {

        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            return "Unknown host";
        }
    }



    @RequestMapping("/")
    public String index() {
        return "home";
    }

}
