package org.oasis_eu.portal.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.model.subscription.Subscription;
import org.oasis_eu.portal.core.mongo.dao.my.DashboardRepository;
import org.oasis_eu.portal.core.mongo.model.my.UserContext;
import org.oasis_eu.portal.main.OasisPortal;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.oasis_eu.spring.test.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = {OasisPortal.class})
@Category(IntegrationTest.class)
public class PortalDashboardServiceTest {

    // well-known "alice" user id
    public static final String USER_ID = "bb2c6f76-362f-46aa-982c-1fc60d54b8ef";


    @Autowired
    private PortalDashboardService portalDashboardService;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private SubscriptionStore subscriptionStore;

    @Before
    public void setUp() {
        UserInfo dummy = new UserInfo();
        dummy.setUserId(USER_ID);

        OpenIdCAuthentication auth = mock(OpenIdCAuthentication.class);
        when(auth.getUserInfo()).thenReturn(dummy);

        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        dashboardRepository.deleteAll();
    }

    @Test
    public void testDashboardService() {

        UserContext userContext = portalDashboardService.getPrimaryUserContext();
        String userContextId = userContext.getId();
        List<Subscription> subscriptions = subscriptionStore.findByUserId(USER_ID);
        assertNotEquals(0, subscriptions.size());
        assertEquals(subscriptions.size(), portalDashboardService.getDashboardEntries(userContextId).size());
    }

}
