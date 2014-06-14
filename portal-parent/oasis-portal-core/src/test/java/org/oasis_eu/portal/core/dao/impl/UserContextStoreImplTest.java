package org.oasis_eu.portal.core.dao.impl;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.dao.UserContextStore;
import org.oasis_eu.portal.core.model.subscription.UserContext;
import org.oasis_eu.spring.config.KernelConfiguration;
import org.oasis_eu.spring.test.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DAOTestConfiguration.class, loader = AnnotationConfigContextLoader.class)
@Category(IntegrationTest.class)
public class UserContextStoreImplTest {

    public static final String USER_ID = "bb2c6f76-362f-46aa-982c-1fc60d54b8ef";



    @Autowired
    private UserContextStore store;

    @Test
    public void testGetUserContexts() throws Exception {

        assertEquals(1, store.getUserContexts(USER_ID).size());

    }

    @Test
    public void testCreateDeleteUserContext() {
        UserContext context = new UserContext();
        context.setName("Pro");
        UserContext uc = store.addUserContext(USER_ID, context);
        List<UserContext> contexts = store.getUserContexts(USER_ID);
        assertEquals(2, contexts.size());
        assertTrue(contexts.stream().anyMatch(c -> c.getName().equals("Pro")));

        store.deleteUserContext(USER_ID, uc.getId());

        contexts = store.getUserContexts(USER_ID);
        assertEquals(1, contexts.size());

    }
}
