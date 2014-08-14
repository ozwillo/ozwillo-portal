package org.oasis_eu.portal.front.my.appsmanagement;

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

/**
 * User: schambon
 * Date: 8/13/14
 */
@Controller
@RequestMapping("/my/appsmanagement/subscription-settings/{service_id}")
public class ServiceUsersController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUsersController.class);

    @Autowired
    private PortalAppManagementService appManagementService;

    @RequestMapping(method = RequestMethod.GET)
    public String get(Model model, @PathVariable("service_id") String serviceId) {

        model.addAttribute("service", appManagementService.getService(serviceId));

        return "appsmanagement/assign-service::main";
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/users")
    public List<Users> loadUsers(@PathVariable("service_id") String serviceId) {
        Users u1 = new Users();
        u1.fullname = "Frank Llyod Wright";
        u1.userid = "flw";
        Users u2 = new Users();
        u2.fullname = "George of the Jungle";
        u2.userid = "uj";
        return Arrays.asList(u1, u2);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(method = RequestMethod.POST, value = "/users")
    public void saveUsers(@RequestBody List<Users> users, @PathVariable("service_id") String serviceId) {
        users.forEach(u -> logger.debug("Added user {} with full name {}", u.getUserid(), u.getFullname()));

    }


    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/users/all")
    public List<Users> loadAllUsers(@PathVariable("service_id") String serviceId) {
        Users u1 = new Users();
        u1.fullname = "Leto Atreides";
        u1.userid = "leto";
        Users u2 = new Users();
        u2.fullname = "Philippe le Bel";
        u2.userid = "philippe";
        Users u3 = new Users();
        u3.fullname = "Louis le Grand";
        u3.userid = "louis";

        return Arrays.asList(u1, u2, u3);
    }

    @ExceptionHandler(WrongQueryException.class)
    public String handleWrongQuery() {
        return "appsmanagement/apps-byauth::forbidden";
    }

    @ExceptionHandler(TechnicalErrorException.class)
    public String handleTechnicalError() {
        return "appsmanagement/apps-byauth::technical_error";
    }

    public static class Users {
        String fullname;
        String userid;

        public String getFullname() {
            return fullname;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }
    }
}
