package org.oasis_eu.portal.front.my.appsmanagement;

import org.oasis_eu.portal.model.appsmanagement.User;
import org.oasis_eu.portal.services.PortalAppManagementService;
import org.oasis_eu.spring.kernel.exception.TechnicalErrorException;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 8/13/14
 */
@Deprecated
@Controller
@RequestMapping("/my/appsmanagement/subscription-settings")
public class ServiceUsersController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUsersController.class);

    @Autowired
    private PortalAppManagementService appManagementService;

    @RequestMapping(method = RequestMethod.GET, value ="/{service_id}")
    public String get(Model model, @PathVariable("service_id") String serviceId) {

//        model.addAttribute("service", appManagementService.getService(serviceId));

        return "appsmanagement/assign-service::main";
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/{service_id}/users")
    public List<User> loadUsers(@PathVariable("service_id") String serviceId) {
        return appManagementService.getSubscribedUsersOfService(serviceId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(method = RequestMethod.POST, value = "/{service_id}/users")
    public void saveUsers(@RequestBody List<User> users, @PathVariable("service_id") String serviceId) {
        users.forEach(u -> logger.debug("Added user {} with full name {}", u.getUserid(), u.getFullname()));

        Set<String> userIds = users.stream().map(User::getUserid).collect(Collectors.toSet());

        Set<String> alreadySubscribed = appManagementService.getSubscribedUsersOfService(serviceId).stream().map(User::getUserid).collect(Collectors.toSet());

        Set<String> toAdd = userIds.stream().filter(u -> !alreadySubscribed.contains(u)).collect(Collectors.toSet());
        appManagementService.subscribeUsers(toAdd, serviceId);

        Set<String> toRemove = alreadySubscribed.stream().filter(s -> !userIds.contains(s)).collect(Collectors.toSet());
        appManagementService.unsubscribeUsers(toRemove, serviceId);

    }


    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/{service_id}/users/all")
    public List<User> loadAllUsers(@PathVariable("service_id") String serviceId) {
        return appManagementService.getAllUsersOfServiceOrganization(serviceId);
    }

    @ExceptionHandler(WrongQueryException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleWrongQuery() {
        return "appsmanagement/apps-byauth::forbidden";
    }

    @ExceptionHandler(TechnicalErrorException.class)
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public String handleTechnicalError() {
        return "appsmanagement/apps-byauth::technical_error";
    }

}
