package org.oasis_eu.portal.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.mongo.dao.my.DashboardRepository;
import org.oasis_eu.portal.core.mongo.model.my.UserContext;
import org.oasis_eu.portal.main.OasisPortal;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.test.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {OasisPortal.class})
@Category(IntegrationTest.class)
public class PortalDashboardServiceTest {



    // well-known "alice" user id
    public static final String USER_ID = "bb2c6f76-362f-46aa-982c-1fc60d54b8ef";


    @Autowired
    private PortalDashboardService portalDashboardService;

    @Autowired
    private PortalNotificationService notificationService;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Before
    public void setUp() {
        UserInfo dummy = new UserInfo();
        dummy.setUserId(USER_ID);

        UserInfoHelper helper = mock(UserInfoHelper.class);
        when(helper.currentUser()).thenReturn(dummy);

        ReflectionTestUtils.setField(portalDashboardService, "userInfoHelper", helper);
        ReflectionTestUtils.setField(notificationService, "userInfoHelper", helper);

        dashboardRepository.deleteAll();

    }

    @Test
    public void testDashboardService() {
        UserContext userContext = portalDashboardService.getPrimaryUserContext();
        String userContextId = userContext.getId();
        List<Subscription> subscriptions = subscriptionStore.findByUserId(USER_ID);
        assertNotEquals(0, subscriptions.size());
        assertEquals(subscriptions.size(), portalDashboardService.getDashboardEntries(userContextId, Locale.ENGLISH).size());
    }

}
