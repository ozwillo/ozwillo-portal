package org.oasis_eu.portal.home;

import org.oasis_eu.portal.core.services.cms.ContentRenderingService;
import org.oasis_eu.portal.generic.PortalController;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * User: schambon
 * Date: 5/13/14
 */
@Controller
public class HomeController extends PortalController {

    @Autowired
    private ContentRenderingService contentRenderingService;

    @ModelAttribute("home_content")
    public String content(HttpServletRequest request) {
        Locale locale = RequestContextUtils.getLocale(request);
        return contentRenderingService.render("home", locale);
    }

    @RequestMapping("/")
    public String index() {
        return "home";
    }

}
