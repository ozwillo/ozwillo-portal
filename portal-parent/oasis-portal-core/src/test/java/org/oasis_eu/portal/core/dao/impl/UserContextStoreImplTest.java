package org.oasis_eu.portal.core.dao.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.dao.UserContextStore;
import org.oasis_eu.portal.core.model.subscription.UserContext;
import org.oasis_eu.spring.config.KernelConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = UserContextStoreImplTest.class, loader = AnnotationConfigContextLoader.class)
@Configuration
@Import(KernelConfiguration.class)
@ComponentScan(basePackages = "org.oasis_eu.portal.core")
@PropertySource("classpath:test-application.properties")
public class UserContextStoreImplTest {

    public static final String USER_ID = "bb2c6f76-362f-46aa-982c-1fc60d54b8ef";

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Autowired
    private UserContextStore store;

    @Test
    public void testGetUserContexts() throws Exception {

        assertEquals(1, store.getUserContexts(USER_ID).size());

    }

    @Test
    public void testCreateUserContext() {
        UserContext context = new UserContext();
        context.setName("Pro");
        store.addUserContext(USER_ID, context);
        List<UserContext> contexts = store.getUserContexts(USER_ID);
        assertEquals(2, contexts.size());
        assertTrue(contexts.stream().anyMatch(c -> c.getName().equals("Pro")));

    }
}