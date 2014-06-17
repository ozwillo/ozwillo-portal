package org.oasis_eu.portal.core.dao.impl;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.subscription.ApplicationType;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.spring.test.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DAOTestConfiguration.class, loader = AnnotationConfigContextLoader.class)
@Category(IntegrationTest.class)
public class SubscriptionStoreImplTest {

    public static final String USER_ID = "bb2c6f76-362f-46aa-982c-1fc60d54b8ef";

    @Autowired
    private SubscriptionStore store;

    @Test
    public void testGetSubscriptions() {
        List<Subscription> subscriptions = store.findByUserId(USER_ID);
        assertEquals(2, subscriptions.size());

        assertTrue(subscriptions.stream().anyMatch(s -> s.getApplicationId().equals("elecRollValence") && s.getApplicationType().equals(ApplicationType.LOCAL_SERVICE)));

    }

}
