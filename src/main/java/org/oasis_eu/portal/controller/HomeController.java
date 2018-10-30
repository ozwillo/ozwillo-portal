package org.oasis_eu.portal.controller;

import org.oasis_eu.portal.config.environnements.EnvProperties;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.oasis_eu.portal.services.EnvPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @Autowired
    private EnvPropertiesService envPropertiesService;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping("/")
    public ResponseEntity<?> index() {
        EnvConfig envConfig = envPropertiesService.getConfig(request.getServerName());
        HttpHeaders headers = new HttpHeaders();
        // Let the website handle the display language based on which he knows and our browsing preferences
        headers.add("Location", envConfig.getWeb().getHome());

        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }
}
