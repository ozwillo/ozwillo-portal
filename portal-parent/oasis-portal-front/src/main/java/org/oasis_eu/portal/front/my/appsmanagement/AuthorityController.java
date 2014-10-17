package org.oasis_eu.portal.front.my.appsmanagement;

import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.front.generic.PortalController;
import org.oasis_eu.portal.model.appsmanagement.Authority;
import org.oasis_eu.portal.services.NetworkService;
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
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Used for AJAX calls
 *
 * User: schambon
 * Date: 8/13/14
 */
@Controller
@RequestMapping("/my/appsmanagement")
public class AuthorityController extends PortalController {

    @Autowired
    private PortalAppManagementService appManagementService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private NetworkService networkService;


    @RequestMapping(method = RequestMethod.GET, value = "/service-settings/{service_id}")
    public String serviceSettings(Model model, @PathVariable("service_id") String serviceId, HttpServletRequest httpRequest) {

        CatalogEntry service = appManagementService.getService(serviceId);
        model.addAttribute("service", service);
        model.addAttribute("iconUrl", imageService.getImageForURL(service.getIcon(RequestContextUtils.getLocale(httpRequest)), ImageFormat.PNG_64BY64, false));

        return "appsmanagement/service-settings::main";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/authority/{authority_type}/{authority_id}")
    public String getAuthority(Model model, @PathVariable("authority_type") String authorityType, @PathVariable("authority_id") String authorityId) {
        Authority authority = networkService.getAuthority(authorityType, authorityId);

        if (authority == null) {
            return "appsmanagement/apps-byauth::forbidden";
        }

        model.addAttribute("authority", authority);
        model.addAttribute("instances", appManagementService.getMyInstances(authority));

        return "appsmanagement/apps-byauth::authority";
    }

    @RequestMapping(method = RequestMethod.GET, value="/delete-instance/{instanceId}")
    public String deleteInstance(@PathVariable String instanceId) {
        if (getDevMode()) {
            appManagementService.deleteInstance(instanceId);
        }
        return "redirect:/my/appsmanagement";
    }

    @ExceptionHandler(WrongQueryException.class)
    public String handleWrongQuery() {
        return "appsmanagement/apps-byauth::forbidden";
    }

    @ExceptionHandler(TechnicalErrorException.class)
    public String handleTechnicalError() {
        return "appsmanagement/apps-byauth::technical_error";
    }

}
