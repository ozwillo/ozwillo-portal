package org.oasis_eu.portal.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oasis_eu.portal.dao.RatingRepository;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: schambon
 * Date: 10/31/14
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class RatingServiceTest {

	static Logger logger = LoggerFactory.getLogger(RatingServiceTest.class);

	@Autowired
	private RatingRepository repository;

	@Autowired
	private RatingService ratingService;

	@BeforeEach
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

	@Test
	public void testMultipleRating() {
		setAuthentication("toto");
		ratingService.rate("application", "citizenkin", 3);
		assertThrows(IllegalArgumentException.class, () -> ratingService.rate("application", "citizenkin", 4));
	}

	@Test
	public void testRatingTooLow() {
		setAuthentication("toto");
		assertThrows(IllegalArgumentException.class, () -> ratingService.rate("application", "citizenkin", -1));
	}

	@Test
	public void testRatingTooHigh() {
		setAuthentication("toto");
		assertThrows(IllegalArgumentException.class, () -> ratingService.rate("application", "citizenkin", 6));
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
