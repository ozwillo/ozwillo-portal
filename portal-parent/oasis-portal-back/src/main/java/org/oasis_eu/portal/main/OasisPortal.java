package org.oasis_eu.portal.main;

import org.oasis_eu.portal.PortalPackage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * User: schambon
 * Date: 5/13/14
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan(basePackageClasses = PortalPackage.class)
public class OasisPortal {

    public static void main(String[] args) {
        SpringApplication.run(OasisPortal.class);
    }
}
