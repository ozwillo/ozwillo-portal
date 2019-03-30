package org.oasis_eu.portal.controller;


import org.oasis_eu.portal.services.EnvPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/env")
public class EnvPropertiesController {

    @Autowired
    private EnvPropertiesService envPropertiesService;

    @GetMapping()
    public String envPropertyName(){
        return envPropertiesService.getCurrentKey();
    }
}
