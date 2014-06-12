package org.oasis_eu.portal.front.home;

import org.oasis_eu.portal.core.services.cms.ContentRenderingService;
import org.oasis_eu.portal.front.generic.PortalController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
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
