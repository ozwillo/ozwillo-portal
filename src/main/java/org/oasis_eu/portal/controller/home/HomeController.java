package org.oasis_eu.portal.controller.home;

import org.oasis_eu.portal.controller.generic.PortalController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * User: schambon
 * Date: 5/13/14
 */
@Controller
public class HomeController extends PortalController {

    @Value("${web.home}")
    private String webHome;

    @RequestMapping("/")
    public ResponseEntity<?> index(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        // Let the website handle the display language based on which he knows and our browsing preferences
        headers.add("Location", webHome);

        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }
}
