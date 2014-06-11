package org.oasis_eu.portal.cms;

import org.oasis_eu.portal.generic.PortalController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * User: schambon
 * Date: 5/15/14
 */
@Controller
public class ContentController extends PortalController {

    @RequestMapping("/{page}/{content}.html")
    public String contentPage(@PathVariable String page, @PathVariable String content, Model model, HttpServletResponse response) {
        model.addAttribute("content", content);
        model.addAttribute("page", page);

        response.setHeader("Cache-Control", "public");

        return "content";
    }

}
