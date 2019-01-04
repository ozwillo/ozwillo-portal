package org.oasis_eu.portal.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthProvider;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * User: schambon
 * Date: 9/1/14
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext
public class TestTokenRefresh {

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private Filter springSecurityFilterChain;

	@Autowired
	private OpenIdCAuthProvider openIdCAuthProvider;

	private MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		mockMvc = MockMvcBuilders
				.webAppContextSetup(wac)
				.addFilters(springSecurityFilterChain)
				.build();

		// force not loading user info from the Kernel
		openIdCAuthProvider.setFetchUserInfo(false);
	}


	/**
	 * Test that the access token's being expired correctly, that is to say the interceptor throws a RefreshTokenNeededException
	 * (wrapped in a NestedServletException as may be).
	 * Note that we get the exception rather than a redirect to
	 * @throws Throwable
	 */
	@Test
	public void testExpiry() throws Exception {


		OpenIdCAuthentication auth = new OpenIdCAuthentication("-test-subject-", "-test-accesstoken-", "-test-idtoken-", Instant.now(), Instant.now().plus(3, ChronoUnit.SECONDS), true, false);
		SecurityContextHolder.getContext().setAuthentication(auth);

		MockHttpSession session = new MockHttpSession();
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

		mockMvc.perform(get("/my").session(session)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("http://localhost/login?ui_locales=en"));


	}



}
