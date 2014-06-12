package org.oasis_eu.portal.mockserver.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * User: schambon
 * Date: 6/12/14
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.oasis_eu.portal")
public class MockServer {

    public static void main(String[] args) {
        SpringApplication.run(MockServer.class, args);
    }

}
