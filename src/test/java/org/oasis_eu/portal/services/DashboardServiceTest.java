package org.oasis_eu.portal.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.dao.SubscriptionStore;
import org.oasis_eu.portal.core.mongo.dao.my.DashboardRepository;
import org.oasis_eu.portal.core.mongo.model.my.UserContext;
import org.oasis_eu.portal.OasisPortal;
import org.oasis_eu.spring.kernel.model.UserInfo;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = {OasisPortal.class, MockServletContext.class})
public class DashboardServiceTest {

	// well-known "alice" user id
	public static final String USER_ID = "bb2c6f76-362f-46aa-982c-1fc60d54b8ef";

	@Autowired
	private RestTemplate kernelRestTemplate;

	@Autowired
	private DashboardService dashboardService;

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

		String response = "[{\"id\":\"6bbb4729-ab6e-4c91-8b62-914e5de5cf12\",\"subscription_uri\":\"https://oasis-demo.atolcd.com/apps/subscriptions/subscription/6bbb4729-ab6e-4c91-8b62-914e5de5cf12\",\"subscription_etag\":\"1409763694748\",\"service_id\":\"4de8452c-043f-401b-882f-1a489cb22743\",\"service_name\":\"Pré-inscription sur liste électorale\",\"service_name#fr\":\"Pré-inscription sur liste électorale\",\"service_name#bg\":\"Pré-inscription sur liste électorale\",\"service_name#tr\":\"Pré-inscription sur liste électorale\",\"service_name#ca\":\"Pré-inscription sur liste électorale\",\"service_name#en\":\"Pré-inscription sur liste électorale\",\"service_name#es\":\"Pré-inscription sur liste électorale\",\"service_name#it\":\"Pré-inscription sur liste électorale\",\"subscription_type\":\"PERSONAL\",\"creator_id\":\"a399684b-4ea3-49c3-800b-b8a0bf1131cb\",\"creator_name\":\"John Doe\"},{\"id\":\"d566137f-3434-4743-81c3-118dc6192308\",\"subscription_uri\":\"https://oasis-demo.atolcd.com/apps/subscriptions/subscription/d566137f-3434-4743-81c3-118dc6192308\",\"subscription_etag\":\"1409763765222\",\"service_id\":\"f5801113-db1e-4d89-afcf-7a1d19555eb3\",\"service_name\":\"Citizen Procedures Management - Back office\",\"service_name#fr\":\"Citizen Procedures Management - Back office\",\"service_name#bg\":\"Citizen Procedures Management - Back office\",\"service_name#tr\":\"Citizen Procedures Management - Back office\",\"service_name#ca\":\"Citizen Procedures Management - Back office\",\"service_name#en\":\"Citizen Procedures Management - Back office\",\"service_name#es\":\"Citizen Procedures Management - Back office\",\"service_name#it\":\"Citizen Procedures Management - Back office\",\"subscription_type\":\"ORGANIZATION\",\"creator_id\":\"a399684b-4ea3-49c3-800b-b8a0bf1131cb\",\"creator_name\":\"John Doe\"}]";

		MockRestServiceServer mock = MockRestServiceServer.createServer(kernelRestTemplate);

		mock.expect(requestTo("http://localhost:8081/subs/user/bb2c6f76-362f-46aa-982c-1fc60d54b8ef"))
				.andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

		mock.expect(requestTo("http://localhost:8081/apps/service/4de8452c-043f-401b-882f-1a489cb22743"))
				.andRespond(withSuccess("{\"name\":\"Pré-inscription sur liste électorale\",\"name#fr\":\"Pré-inscription sur liste électorale\",\"name#bg\":\"Pré-inscription sur liste électorale\",\"name#tr\":\"Pré-inscription sur liste électorale\",\"name#ca\":\"Pré-inscription sur liste électorale\",\"name#en\":\"Pré-inscription sur liste électorale\",\"name#es\":\"Pré-inscription sur liste électorale\",\"name#it\":\"Pré-inscription sur liste électorale\",\"description\":\"Pré-inscription sur liste électorale\",\"description#fr\":\"Pré-inscription sur liste électorale\",\"description#bg\":\"Pré-inscription sur liste électorale\",\"description#tr\":\"Pré-inscription sur liste électorale\",\"description#ca\":\"Pré-inscription sur liste électorale\",\"description#en\":\"Pré-inscription sur liste électorale\",\"description#es\":\"Pré-inscription sur liste électorale\",\"description#it\":\"Pré-inscription sur liste électorale\",\"icon\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#fr\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#bg\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#tr\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#ca\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#en\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#es\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#it\":\"http://localhost:8000/sample-connecte-17.png\",\"provider_id\":\"a2342900-f9eb-4d54-bf30-1e0d763ec4af\",\"payment_option\":\"FREE\",\"target_audience\":[\"CITIZENS\"],\"visible\":true,\"local_id\":\"electoral_roll_registration\",\"instance_id\":\"517fda66-0371-4c19-8698-359ca32f2bf2\",\"service_uri\":\"http://localhost:9090/front/valence/form/electoral_roll_registration/init\",\"notification_uri\":\"http://localhost:9090/front/valence/notifications\",\"redirect_uris\":[\"http://localhost:9090/front/callback\",\"http://localhost:9090/front/valence/form/electoral_roll_registration/oasis_profile_callback\"],\"post_logout_redirect_uris\":[\"http://localhost:9090/front\"],\"territory_id\":\"26000\",\"type\":\"SERVICE\",\"id\":\"4de8452c-043f-401b-882f-1a489cb22743\",\"modified\":1409738554712}",
						MediaType.APPLICATION_JSON));

		mock.expect(requestTo("https://oasis-demo.atolcd.com/n/bb2c6f76-362f-46aa-982c-1fc60d54b8ef/messages?instance=4de8452c-043f-401b-882f-1a489cb22743&status=UNREAD"))
				.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

		mock.expect(requestTo("http://localhost:8081/apps/service/f5801113-db1e-4d89-afcf-7a1d19555eb3"))
				.andRespond(withSuccess("{\"name\":\"Citizen Procedures Management - Back office\",\"name#fr\":\"Citizen Procedures Management - Back office\",\"name#bg\":\"Citizen Procedures Management - Back office\",\"name#tr\":\"Citizen Procedures Management - Back office\",\"name#ca\":\"Citizen Procedures Management - Back office\",\"name#en\":\"Citizen Procedures Management - Back office\",\"name#es\":\"Citizen Procedures Management - Back office\",\"name#it\":\"Citizen Procedures Management - Back office\",\"description\":\"Management tool for citizen procedures\",\"description#fr\":\"Management tool for citizen procedures\",\"description#bg\":\"Management tool for citizen procedures\",\"description#tr\":\"Management tool for citizen procedures\",\"description#ca\":\"Management tool for citizen procedures\",\"description#en\":\"Management tool for citizen procedures\",\"description#es\":\"Management tool for citizen procedures\",\"description#it\":\"Management tool for citizen procedures\",\"icon\":\"http://localhost:8000/sample-connecte-16.png\",\"icon#fr\":\"http://localhost:8000/sample-connecte-16.png\",\"icon#bg\":\"http://localhost:8000/sample-connecte-16.png\",\"icon#tr\":\"http://localhost:8000/sample-connecte-16.png\",\"icon#ca\":\"http://localhost:8000/sample-connecte-16.png\",\"icon#en\":\"http://localhost:8000/sample-connecte-16.png\",\"icon#es\":\"http://localhost:8000/sample-connecte-16.png\",\"icon#it\":\"http://localhost:8000/sample-connecte-16.png\",\"provider_id\":\"a2342900-f9eb-4d54-bf30-1e0d763ec4af\",\"payment_option\":\"PAID\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":false,\"local_id\":\"back\",\"instance_id\":\"517fda66-0371-4c19-8698-359ca32f2bf2\",\"service_uri\":\"http://localhost:9090/back/valence\",\"notification_uri\":\"http://localhost:9090/back/valence/notifications\",\"redirect_uris\":[\"http://localhost:9090/back/login\"],\"post_logout_redirect_uris\":[\"http://localhost:9090/back\"],\"territory_id\":\"26000\",\"type\":\"SERVICE\",\"id\":\"f5801113-db1e-4d89-afcf-7a1d19555eb3\",\"modified\":1409763726327}",
						MediaType.APPLICATION_JSON));

		mock.expect(requestTo("https://oasis-demo.atolcd.com/n/bb2c6f76-362f-46aa-982c-1fc60d54b8ef/messages?instance=f5801113-db1e-4d89-afcf-7a1d19555eb3&status=UNREAD"))
				.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

		UserContext userContext = dashboardService.getPrimaryUserContext();
		String userContextId = userContext.getId();

//		assertEquals(2, portalDashboardService.getDashboardEntries(userContextId).size());
	}

}
