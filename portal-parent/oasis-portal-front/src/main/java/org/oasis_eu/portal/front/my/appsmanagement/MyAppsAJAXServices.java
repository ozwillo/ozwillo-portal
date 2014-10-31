package org.oasis_eu.portal.front.my.appsmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.portal.core.model.catalog.CatalogEntry;
import org.oasis_eu.portal.core.mongo.model.images.ImageFormat;
import org.oasis_eu.portal.core.services.icons.ImageService;
import org.oasis_eu.portal.model.appsmanagement.Authority;
import org.oasis_eu.portal.model.appsmanagement.MyAppsInstance;
import org.oasis_eu.portal.model.appsmanagement.MyAppsService;
import org.oasis_eu.portal.model.appsmanagement.User;
import org.oasis_eu.portal.services.NetworkService;
import org.oasis_eu.portal.services.PortalAppManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: schambon
 * Date: 10/21/14
 */
@RestController
@RequestMapping("/my/api/myapps")
public class MyAppsAJAXServices {

    private static final Logger logger = LoggerFactory.getLogger(MyAppsAJAXServices.class);

    @Autowired
    private NetworkService networkService;

    @Autowired
    private PortalAppManagementService appManagementService;

    @Autowired
    private ImageService imageService;

    @Value("${application.devmode:false}")
    private boolean devmode;

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

        List<MyAppsInstance> myInstances = appManagementService.getMyInstances(networkService.getAuthority(strings[0], strings[1]));
        for (MyAppsInstance instance : myInstances) {
            instance.setIcon(imageService.getImageForURL(instance.getApplication().getIcon(), ImageFormat.PNG_64BY64, false));
        }
        return myInstances;
    }

    @RequestMapping(value = "/users/instance/{instanceId}", method = RequestMethod.GET)
    public List<User> getUsersForInstance(@PathVariable String instanceId, @RequestParam(required = false) String q) {
        List<User> appUsers = appManagementService.getAppUsers(instanceId);
        logger.debug("Found appusers: {} for instance {}", appUsers, instanceId);
        if (q == null) {
            return appUsers;
        } else {
            return appUsers.stream().filter(u -> u.getFullname().toLowerCase().contains(q.toLowerCase())).collect(Collectors.toList());
        }
    }

    @RequestMapping(value = "/users/instance/{instanceId}", method = RequestMethod.POST)
    public Map<String,String> setUsersForInstance(@PathVariable String instanceId, @RequestBody List<User> users) {
        appManagementService.saveAppUsers(instanceId, users.stream().map(User::getUserid).collect(Collectors.toList()));
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
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/users/service/{serviceId}", method = RequestMethod.GET)
    public List<User> getUsersForService(@PathVariable String serviceId) {
        return appManagementService.getSubscribedUsersOfService(serviceId);
    }

    @RequestMapping(value = "/users/service/{serviceId}", method = RequestMethod.POST)
    public Map<String, String> saveUsersForService(@PathVariable String serviceId, @RequestBody List<User> users) {
        appManagementService.updateSubscriptions(serviceId, users.stream().map(User::getUserid).collect(Collectors.toSet()));
        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "OK");
        return result;
    }

    @RequestMapping(value = "/service/{serviceId}", method = RequestMethod.GET)
    public MyAppsService loadService(@PathVariable String serviceId) {
        return appManagementService.getService(serviceId);
    }

    @RequestMapping(value = "/service/{serviceId}", method = RequestMethod.POST)
    public SaveServiceResponse saveService(@PathVariable String serviceId, @RequestBody @Valid CatalogEntry entry, Errors errors) {
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
            appManagementService.updateService(serviceId, entry);

            response.success = true;
        }

        return response;
    }

    @RequestMapping(value = "/deprovision/{instanceId}", method = RequestMethod.POST)
    public void deprovision(@PathVariable String instanceId) {
        if (devmode) {
            appManagementService.deleteInstance(instanceId);

        }

    }

    public static class SaveServiceResponse {
        @JsonProperty boolean success;
        @JsonProperty List<String> errors;
    }


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void accessDenied() {}

}
