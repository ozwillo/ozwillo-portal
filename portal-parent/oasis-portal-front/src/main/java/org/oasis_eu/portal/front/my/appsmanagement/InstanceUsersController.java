package org.oasis_eu.portal.front.my.appsmanagement;

import org.oasis_eu.portal.model.appsmanagement.User;
import org.oasis_eu.portal.services.PortalAppManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 9/16/14
 */
@Controller
@RequestMapping("/my/appsmanagement/settings")
public class InstanceUsersController {

    @Autowired
    private PortalAppManagementService appManagementService;

    @RequestMapping(method = RequestMethod.GET, value = "/{org_id}/{app_id}")
    public String appSettings(Model model, @PathVariable("org_id") String organizationId, @PathVariable("app_id") String instanceId) {
        model.addAttribute("instance", appManagementService.getInstance(instanceId)); // note that we trust the Kernel's security on that one
        model.addAttribute("orgid", organizationId);

        return "appsmanagement/instance-settings";
    }


    @RequestMapping("/{org_id}/{app_id}/app_users")
    @ResponseBody
    public List<User> getAppUsers(@PathVariable("app_id") String instanceId) {
        return appManagementService.getAppUsers(instanceId);
    }

    @RequestMapping("/{org_id}/{app_id}/org_users")
    @ResponseBody
    public List<User> getOrganizationUsers(@PathVariable("org_id") String organizationId) {
        return appManagementService.getUsersOfOrganization(organizationId);
    }

    @RequestMapping("/{org_id}/{app_id}/save")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void save(@RequestBody User[] users, @PathVariable("org_id") String organizationId, @PathVariable("app_id") String instanceId) {
        appManagementService.saveAppUsers(instanceId,
                Arrays.asList(users).stream()
                        .map(User::getUserid)
                        .collect(Collectors.toList()));
    }
}
