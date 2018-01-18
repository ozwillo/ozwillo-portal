package org.oasis_eu.portal.front.my.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import org.oasis_eu.portal.core.model.catalog.ServiceEntry;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.front.generic.BaseController;
import org.oasis_eu.portal.model.authority.Authority;
import org.oasis_eu.portal.model.app.instance.MyAppsInstance;
import org.oasis_eu.portal.model.app.service.Service;
import org.oasis_eu.portal.model.user.User;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.portal.services.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * User: schambon
 * Date: 10/21/14
 */
@RestController
@RequestMapping("/my/api/myapps")
public class MyAppsAJAXServices extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MyAppsAJAXServices.class);

    @Autowired
    private NetworkService networkService;

    @Autowired
    private ApplicationService appManagementService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping("/authorities")
    public List<Authority> getAuthorities() {
        return networkService.getMyAuthorities(true).stream()
            .filter(Authority::isAdmin)
            .map(a -> new Authority(a.getType(), a.getName(), a.getType() + "::" + a.getId(), a.isAdmin()))
            .collect(Collectors.toList());
    }

    @RequestMapping("/instances/{authorityId}")
    public List<MyAppsInstance> getInstances(@PathVariable String authorityId) {
        String[] strings = authorityId.split("::");

        List<MyAppsInstance> myInstances =
            appManagementService.getMyInstances(networkService.getAuthority(strings[0], strings[1]), true);
        for (MyAppsInstance instance : myInstances) {
            instance.setIcon(
                imageService.getImageForURL(instance.getApplicationInstance().getIcon(RequestContextUtils.getLocale(request)),
                    ImageFormat.PNG_64BY64, false));
            instance.setName(instance.getApplicationInstance().getName(RequestContextUtils.getLocale(request)));
        }

        return myInstances;
    }

    /**
     * Used to 1. list users that can be added to app instance (with !appAdmin since app admins
     * will only be manageable individually later, for now any app admins = the app's orga admins)
     * and 2. to list users that can be subscribed to service (with appAdmin)
     *
     * @param instanceId
     * @param q          the query string
     * @param appAdmin   whether to also return users that are app_admin (and not app_user), default is true
     * @return app instance Users i.e. that are app_user
     */
    @RequestMapping(value = "/users/instance/{instanceId}", method = RequestMethod.GET)
    public List<User> getUsersForInstance(@PathVariable String instanceId,
        @RequestParam(required = false) String q,
        @RequestParam(value = "app_admin", required = false, defaultValue = "true") boolean appAdmin,
        @RequestParam(value = "pending", required = false, defaultValue = "false") boolean pending) {
        List<User> appUsers = appManagementService.getAppUsers(instanceId, appAdmin).stream()
            .sorted(Comparator.comparing(User::getFullname))
            .collect(Collectors.toList());
        if (pending) {
            List<User> pendingUsers = appManagementService.getPendingAppUsers(instanceId).stream()
                .sorted(Comparator.comparing(User::getEmail))
                .collect(Collectors.toList());
            appUsers.addAll(pendingUsers);
        }
        logger.debug("Found {} appusers for instance {}", appUsers.size(), instanceId);
        if (q == null) {
            return appUsers;
        }
        return appUsers.stream().filter(u -> isMatchUser(u, q)).collect(Collectors.toList());
    }

    private static boolean isMatchUser(User u, String query) {
        if (Strings.isNullOrEmpty(u.getFullname())) {
            logger.debug("User without fullname, user id : {}", u.getUserid());
            // See #293 also clean kernel data
            return false;
        }
        return u.getFullname().toLowerCase().contains(query.toLowerCase());
    }

    @RequestMapping(value = "/users/instance/{instanceId}", method = RequestMethod.POST)
    public Map<String, String> setUsersForInstance(@PathVariable String instanceId, @RequestBody List<User> users) {
        appManagementService.saveAppUsers(instanceId, users);
        Map<String, String> result = new HashMap<>();
        result.put("result", "OK");
        return result;
    }

    @RequestMapping("/users/network/{authorityId}")
    public List<User> getUsersForApplication(@PathVariable String authorityId, @RequestParam String q) {
        logger.debug("Getting users with query {}", q);
        String aId = authorityId.split("::")[1];
        return networkService.getUsersOfOrganization(aId)
            .stream()
            .filter(u -> u.getFullname().toLowerCase().contains(q.toLowerCase()))
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * get subscriptions
     *
     * @param serviceId
     * @return users including when app_admin
     */
    @RequestMapping(value = "/users/service/{serviceId}", method = RequestMethod.GET)
    public List<User> getUsersForService(@PathVariable String serviceId) {
        return appManagementService.getSubscribedUsersOfService(serviceId).stream()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * set subscriptions i.e. "save changes" in "push to dashboard" modal
     * (only new subscriptions are pushed to dashboard, so to push to dashboard
     * an existing one it must be removed in a first step)
     *
     * @param serviceId
     * @param users     including when app_admin
     * @return
     */
    @RequestMapping(value = "/users/service/{serviceId}", method = RequestMethod.POST)
    public Map<String, String> saveUsersForService(@PathVariable String serviceId, @RequestBody List<User> users) {
        appManagementService.updateSubscriptions(serviceId, users.stream().map(User::getUserid).collect(Collectors.toSet()));
        Map<String, String> result = new HashMap<>();
        result.put("result", "OK");
        return result;
    }

    @RequestMapping(value = "/service/{serviceId}", method = RequestMethod.GET)
    public Service loadService(@PathVariable String serviceId) {
        return appManagementService.getService(serviceId);
    }

    @RequestMapping(value = "/service/{serviceId}", method = RequestMethod.POST)
    public SaveServiceResponse saveService(@PathVariable String serviceId, @RequestBody @Valid ServiceEntry serviceEntry,
                                           Errors errors) {
        SaveServiceResponse response = new SaveServiceResponse();
        if (errors.hasErrors()) {
            response.errors = errors.getFieldErrors().stream().map(fieldError -> {
                // this is hacky. But we will do better when we support localization
                if (fieldError.getField().startsWith("default")) {
                    return fieldError.getField().substring("default".length()).toLowerCase();
                } else {
                    return fieldError.getField();
                }
            }).collect(Collectors.toList());
            response.success = false;
        } else {
            appManagementService.updateService(serviceId, serviceEntry); // let it explode on a WrongQueryException

            response.success = true;
        }

        return response;
    }

    /**
     * (ideally should be {id}/set-status)
     *
     * @param instance
     * @param instanceId not used
     * @param errors
     * @return
     */
    @RequestMapping(value = "/set-status/{instanceId}", method = POST)
    public String setInstanceStatus(@RequestBody @Valid MyAppsInstance instance, @PathVariable String instanceId,
        Errors errors) {
        logger.debug("Updating app instance {}", instance);

        return appManagementService.setInstanceStatus(instance);
    }

    public static class SaveServiceResponse {
        @JsonProperty
        boolean success;
        @JsonProperty
        List<String> errors;
    }

}
