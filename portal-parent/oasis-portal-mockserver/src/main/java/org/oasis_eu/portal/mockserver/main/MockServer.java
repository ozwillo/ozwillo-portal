package org.oasis_eu.portal.mockserver.main;

import java.util.ArrayList;
import java.util.List;

import org.oasis_eu.portal.mockserver.repo.Repo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 * User: schambon
 * Date: 6/12/14
 */
@Configuration
@EnableAutoConfiguration
@EnableMongoRepositories(basePackageClasses = Repo.class)
@ComponentScan(basePackages = "org.oasis_eu.portal")
@Deprecated
public class MockServer {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        return mapper;
    }


    @Bean
    public MappingJackson2HttpMessageConverter jacksonMessageConverter() {
        MappingJackson2HttpMessageConverter jacksonMessageConverter = new MappingJackson2HttpMessageConverter();
        jacksonMessageConverter.setObjectMapper(objectMapper());
        return jacksonMessageConverter;
    }

    @Bean
    public RestTemplate restTemplate(MappingJackson2HttpMessageConverter jacksonMessageConverter) {
        RestTemplate template = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

//        messageConverters.add(new FormHttpMessageConverter());
//        messageConverters.add(new StringHttpMessageConverter());


        messageConverters.add(jacksonMessageConverter);

        template.setMessageConverters(messageConverters);

        return template;

    }

    public static void main(String[] args) {
        SpringApplication.run(MockServer.class, args);
    }

}
