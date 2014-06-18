package org.oasis_eu.portal.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.main.OasisPortal;
import org.oasis_eu.spring.kernel.model.Address;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.test.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OasisPortal.class)
@Category(IntegrationTest.class)
public class LocalServiceServiceTest {
    // well-known "alice" user id
    public static final String USER_ID = "bb2c6f76-362f-46aa-982c-1fc60d54b8ef";


    @Autowired
    private LocalServiceService localServiceService;

    @Before
    public void setUp() {
        UserInfo dummy = new UserInfo();
        Address address = new Address();
        address.setLocality("Valence");
        address.setPostalCode("26000");
        dummy.setAddress(address);
        dummy.setUserId(USER_ID);

        UserInfoHelper helper = mock(UserInfoHelper.class);
        when(helper.currentUser()).thenReturn(dummy);

        ReflectionTestUtils.setField(localServiceService, "userInfoHelper", helper);

    }

    @Test
    public void findServices() {
        assertEquals(1, localServiceService.findLocalServices().size());
    }
}