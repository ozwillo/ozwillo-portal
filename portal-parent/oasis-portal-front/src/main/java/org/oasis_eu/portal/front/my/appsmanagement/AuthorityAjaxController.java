package org.oasis_eu.portal.front.my.appsmanagement;

import org.oasis_eu.portal.model.appsmanagement.Authority;
import org.oasis_eu.portal.services.PortalAppManagementService;
import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Used for AJAX calls
 *
 * User: schambon
 * Date: 8/13/14
 */
@Controller
@RequestMapping("/my/appsmanagement")
public class AuthorityAjaxController {

    @Autowired
    private PortalAppManagementService appManagementService;


    @RequestMapping(method = RequestMethod.GET, value = "/settings/{app_id}")
    public String appSettings(Model model, @PathVariable("app_id") String instanceId) {
        model.addAttribute("instance", appManagementService.getInstance(instanceId)); // note that we trust the Kernel's security on that one

        return "appsmanagement/instance-settings::main";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/service-settings/{service_id}")
    public String serviceSettings(Model model, @PathVariable("service_id") String serviceId) {

        model.addAttribute("service", appManagementService.getService(serviceId));

        return "appsmanagement/service-settings::main";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/authority/{authority_type}/{authority_id}")
    public String getAuthority(Model model, @PathVariable("authority_type") String authorityType, @PathVariable("authority_id") String authorityId) {
        Authority authority = appManagementService.getAuthority(authorityType, authorityId);

        if (authority == null) {
            return "my-apps-byauth::forbidden";
        }

        model.addAttribute("authority", authority);
        model.addAttribute("instances", appManagementService.getMyInstances(authority));

        return "my-apps-byauth::authority";
    }




    @ExceptionHandler(WrongQueryException.class)
    public String handleWrongQuery() {
        return "my-apps-byauth::forbidden";
    }

    @ExceptionHandler(TechnicalErrorException.class)
    public String handleTechnicalError() {
        return "my-apps-byauth::technical_error";
    }

}
