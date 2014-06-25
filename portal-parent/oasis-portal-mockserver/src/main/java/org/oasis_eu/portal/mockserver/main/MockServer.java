package org.oasis_eu.portal.mockserver.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * User: schambon
 * Date: 6/12/14
 */
@Configuration
@EnableAutoConfiguration
@EnableMongoRepositories
@ComponentScan(basePackages = "org.oasis_eu.portal")
public class MockServer {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        return mapper;
    }

    public static void main(String[] args) {
        SpringApplication.run(MockServer.class, args);
    }

}
