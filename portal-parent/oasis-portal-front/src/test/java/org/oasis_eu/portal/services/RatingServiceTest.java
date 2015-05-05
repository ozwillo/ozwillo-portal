package org.oasis_eu.portal.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.mongo.dao.store.RatingRepository;
import org.oasis_eu.portal.main.OasisPortal;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: schambon
 * Date: 10/31/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = {OasisPortal.class, MockServletContext.class})
public class RatingServiceTest {

    static Logger logger = LoggerFactory.getLogger(RatingServiceTest.class);

    @Autowired
    private RatingRepository repository;

    @Autowired
    private RatingService ratingService;

    @Before
    public void setUp() {
        logger.warn("Emptying database");
        repository.deleteAll();
    }


    private void setAuthentication(String userId) {
        UserInfo dummy = new UserInfo();
        dummy.setUserId(userId);

        OpenIdCAuthentication auth = mock(OpenIdCAuthentication.class);
        when(auth.getUserInfo()).thenReturn(dummy);

        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
    }

    @Test
    public void testRating() {
        setAuthentication("toto");
        ratingService.rate("application", "citizenkin", 4);
        assertEquals(4, ratingService.getRating("application", "citizenkin"), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleRating() {
        setAuthentication("toto");
        ratingService.rate("application", "citizenkin", 3);
        ratingService.rate("application", "citizenkin", 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRatingTooLow() {
        setAuthentication("toto");
        ratingService.rate("application", "citizenkin", -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRatingTooHigh() {
        setAuthentication("toto");
        ratingService.rate("application", "citizenkin", 5);
    }

    @Test
    public void testAverage() {
        setAuthentication("toto");
        ratingService.rate("application", "citizenkin", 4);
        setAuthentication("tata");
        ratingService.rate("application", "citizenkin", 4);
        setAuthentication("titi");
        ratingService.rate("application", "citizenkin", 3);
        setAuthentication("tutu");
        ratingService.rate("application", "citizenkin", 4);

        // so now the average is at 3.75 - the service should respond with 4
        assertEquals(4, ratingService.getRating("application", "citizenkin"), 0.01);

        // another rating at just 3.5 should bring the average to 3.7 - the service now rounds to 3.5
        setAuthentication("tete");
        ratingService.rate("application", "citizenkin", 3.5);
        assertEquals(3.5, ratingService.getRating("application", "citizenkin"), 0.01);
    }
}
