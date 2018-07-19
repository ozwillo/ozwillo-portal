package org.oasis_eu.portal.core.dao.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.dao.kernel.CatalogStoreImpl;
import org.oasis_eu.portal.model.kernel.store.Audience;
import org.oasis_eu.portal.model.kernel.store.CatalogEntry;
import org.oasis_eu.portal.model.kernel.store.PaymentOption;
import org.oasis_eu.portal.model.kernel.store.ServiceEntry;
import org.oasis_eu.spring.config.KernelConfiguration;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = CatalogStoreImplTest.class)
@ComponentScan(basePackages = "org.oasis_eu.portal")
@Import(KernelConfiguration.class)
public class CatalogStoreImplTest {

	@Autowired
	private RestTemplate kernelRestTemplate;

	@Autowired
	private CatalogStoreImpl catalogStore;

	@Before
	public void setupAuthenticationContext() {
		OpenIdCAuthentication authentication = new OpenIdCAuthentication("test", "accesstoken", "idtoken", java.time.Instant.now(), java.time.Instant.now(), true, false);
		SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
		SecurityContextHolder.getContext().setAuthentication(authentication);

	}

	@After
	public void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Before
	public void setupRequestContext() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	@Test
	public void testFindAllVisible() throws Exception {
		int from = 0;

		MockRestServiceServer mock = MockRestServiceServer.createServer(kernelRestTemplate);
		mock.expect(requestTo("http://localhost:8081/catalog/search?start=0&limit=50&target_audience=CITIZENS&payment_option=FREE&payment_option=PAID")).andRespond(withSuccess(
				"[{\"name\":\"Pré-inscription sur liste électorale\",\"name#bg\":\"Pré-inscription sur liste électorale\",\"name#fr\":\"Pré-inscription sur liste électorale\",\"name#ca\":\"Pré-inscription sur liste électorale\",\"name#tr\":\"Pré-inscription sur liste électorale\",\"name#en\":\"Pré-inscription sur liste électorale\",\"name#es\":\"Pré-inscription sur liste électorale\",\"name#it\":\"Pré-inscription sur liste électorale\",\"description\":\"Pré-inscription sur liste électorale\",\"description#bg\":\"Pré-inscription sur liste électorale\",\"description#fr\":\"Pré-inscription sur liste électorale\",\"description#ca\":\"Pré-inscription sur liste électorale\",\"description#tr\":\"Pré-inscription sur liste électorale\",\"description#en\":\"Pré-inscription sur liste électorale\",\"description#es\":\"Pré-inscription sur liste électorale\",\"description#it\":\"Pré-inscription sur liste électorale\",\"icon\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#bg\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#fr\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#ca\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#tr\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#en\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#es\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#it\":\"http://localhost:8000/sample-connecte-17.png\",\"provider_id\":\"a2342900-f9eb-4d54-bf30-1e0d763ec4af\",\"payment_option\":\"FREE\",\"target_audience\":[\"CITIZENS\"],\"visible\":true,\"type\":\"SERVICE\",\"id\":\"4de8452c-043f-401b-882f-1a489cb22743\"},{\"name\":\"Citizen Procedures for Valence\",\"name#bg\":\"Citizen Procedures for Valence\",\"name#fr\":\"Citizen Procedures for Valence\",\"name#ca\":\"Citizen Procedures for Valence\",\"name#tr\":\"Citizen Procedures for Valence\",\"name#en\":\"Citizen Procedures for Valence\",\"name#es\":\"Citizen Procedures for Valence\",\"name#it\":\"Citizen Procedures for Valence\",\"description\":\"Citizen procedures for Valence\",\"description#bg\":\"Citizen procedures for Valence\",\"description#fr\":\"Citizen procedures for Valence\",\"description#ca\":\"Citizen procedures for Valence\",\"description#tr\":\"Citizen procedures for Valence\",\"description#en\":\"Citizen procedures for Valence\",\"description#es\":\"Citizen procedures for Valence\",\"description#it\":\"Citizen procedures for Valence\",\"icon\":\"http://www.openwide.fr\",\"icon#bg\":\"http://www.openwide.fr\",\"icon#fr\":\"http://www.openwide.fr\",\"icon#ca\":\"http://www.openwide.fr\",\"icon#tr\":\"http://www.openwide.fr\",\"icon#en\":\"http://www.openwide.fr\",\"icon#es\":\"http://www.openwide.fr\",\"icon#it\":\"http://www.openwide.fr\",\"provider_id\":\"a2342900-f9eb-4d54-bf30-1e0d763ec4af\",\"payment_option\":\"FREE\",\"target_audience\":[\"CITIZENS\"],\"visible\":true,\"type\":\"SERVICE\",\"id\":\"c4d39816-6462-4405-8571-665c7aecaebb\"}]", MediaType.APPLICATION_JSON));
		List<CatalogEntry> catalogEntries = catalogStore.findAllVisible(Arrays.asList(Audience.CITIZENS),
				Arrays.asList(PaymentOption.FREE, PaymentOption.PAID), new ArrayList<>(0),
                new ArrayList<>(0), new ArrayList<>(0), null, null, from);
		assertEquals(2, catalogEntries.size());
		mock.verify();

		mock = MockRestServiceServer.createServer(kernelRestTemplate);
		mock.expect(requestTo("http://localhost:8081/catalog/search?start=0&limit=50&target_audience=CITIZENS&target_audience=PUBLIC_BODIES&target_audience=COMPANIES&payment_option=FREE&payment_option=PAID")).andRespond(withSuccess(
				"[{\"name\":\"Citizen Kin\",\"description\":\"Gestion de la relation usager\",\"description#en\":\"Citizen form management\",\"icon\":\"http://www-xanadu.citizenkin.com/citizenkin.png\",\"provider_id\":\"6dccdb8d-ec46-4675-9965-806ea37b73e1\",\"payment_option\":\"PAID\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":true,\"type\":\"APPLICATION\",\"id\":\"e5fe75e5-103a-4ce5-8d34-88087520aecb\"},{\"name\":\"City Planning\",\"description\":\"City Planning provides map viewing of the context of regional issues (administrative boundaries, hydrography, road and railway infrastructures), the progress concerning municipal urban planning instruments and planning issues arising from the regulations.\\nThis service runs with public data, provided by local authorities and will help users to identify opportunities to relocate in the territory.\\nCity Planning allows compatibility with data provided by many local authorities from different type of spatial databases.\",\"description#it\":\"City Planning fornisce la visione cartografica del contesto territoriale (confini amministrativi, idrografia, infrastrutture stradali e ferroviarie), i progressi relativi agli strumenti di programmazione urbanistica e comunale, e le questioni di pianificazione derivanti dai piani regolatori.\\nQuesto servizio si basa su dati pubblici, forniti dalle autorità locali e aiuterà gli utenti ad identificare le opportunità per trasferirsi sul territorio.\\nCity Planning permette la compatibilità con i dati forniti da molte autorità locali, con diversi tipi di database spaziali.\",\"icon\":\"http://84.240.187.2/oasis/images/icon/cityplanning.jpg\",\"provider_id\":\"6b39a1cf-b9db-4ef4-b5f6-78df57d33f93\",\"payment_option\":\"FREE\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":true,\"type\":\"APPLICATION\",\"id\":\"8ff61da3-8c57-4a2a-80e7-51bde8034614\"},{\"name\":\"OZmarue\",\"description\":\"OZmarue est un service de déclaration d'incidents dans l'espace public pour les communes qui souhaitent impliquer les citoyens dans la gestion des anomalies rencontrés dans l'espace public.\",\"description#en\":\"OZmarue is a reporting service for public thoroughfare incidents for municipalities that wish to involve citizens.\",\"provider_id\":\"21b1f54e-226e-4b2f-8188-dca975f4970e\",\"payment_option\":\"PAID\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":true,\"type\":\"APPLICATION\",\"id\":\"b0bdaf10-c4d9-442c-93af-97d071ab4111\"},{\"name\":\"Altourism\",\"description\":\"A portal to alternative tourism\",\"description#bg\":\"Портал към алтернативен туризъм\",\"icon\":\"http://test.altourism.zonebg.com/app_icon.png\",\"provider_id\":\"ea596942-c0ac-4b05-b03e-04ac53cc91bd\",\"payment_option\":\"PAID\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":true,\"type\":\"APPLICATION\",\"id\":\"fa38e9f3-e6ba-436c-af50-175c1ba32dec\"},{\"name\":\"OpenElec\",\"description\":\"Gestion des listes électorales\",\"provider_id\":\"077ef0e4-9b90-4d31-b94f-fecc1498047f\",\"payment_option\":\"PAID\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":true,\"type\":\"APPLICATION\",\"id\":\"72C857EF-C913-4F36-8536-811A5C7D351F\"},{\"name\":\"OpenCourrier\",\"description\":\"Gestion des courriers entrants et sortants\",\"provider_id\":\"077ef0e4-9b90-4d31-b94f-fecc1498047f\",\"payment_option\":\"PAID\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":true,\"type\":\"APPLICATION\",\"id\":\"BE10C703-9041-44B1-BDC3-5B985DF2FD4E\"},{\"name\":\"OpenCimetière\",\"description\":\"Gestion des concessions funéraires et des ayant-droits\",\"provider_id\":\"077ef0e4-9b90-4d31-b94f-fecc1498047f\",\"payment_option\":\"PAID\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":true,\"type\":\"APPLICATION\",\"id\":\"F62B1931-6CBB-4BAB-A09D-00395BC0F30B\"},{\"name\":\"On Line Public Purchase\",\"name#ca\":\" Compra Pública En Línia\",\"name#es\":\" Compra Pública En Línea\",\"description\":\"OLPP is a complete tool for managing the tenders issued by a public body in order to cover its public purchasing needs. It includes space for controlling the procedure, for interacting with tenderers and for monitoring times and schedules.\",\"description#ca\":\" CPEL és una eina complerta per la gestió dels processos de compra pública en tot tipus de procediments. Inclou un espai per controlar el procediment , per interactuar amb els licitadors i per controlar terminis i programacions.\",\"description#es\":\"CPEL es una herramienta completa para la gestión de los procesos de compra pública en todo tipo de procedimientos. Incluye un espacio par controlar el procedimiento, per interactuar con los licitadores y para controlar plazos y programaciones;\",\"provider_id\":\"b1eb58d5-7e1a-4088-bcd7-273629a903e8\",\"payment_option\":\"PAID\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":true,\"type\":\"APPLICATION\",\"id\":\"CFD9CCDB-5D26-4A4A-82E7-03E69AA2D83E\"},{\"name\":\"Pré-inscription sur liste électorale\",\"name#bg\":\"Pré-inscription sur liste électorale\",\"name#fr\":\"Pré-inscription sur liste électorale\",\"name#ca\":\"Pré-inscription sur liste électorale\",\"name#tr\":\"Pré-inscription sur liste électorale\",\"name#en\":\"Pré-inscription sur liste électorale\",\"name#es\":\"Pré-inscription sur liste électorale\",\"name#it\":\"Pré-inscription sur liste électorale\",\"description\":\"Pré-inscription sur liste électorale\",\"description#bg\":\"Pré-inscription sur liste électorale\",\"description#fr\":\"Pré-inscription sur liste électorale\",\"description#ca\":\"Pré-inscription sur liste électorale\",\"description#tr\":\"Pré-inscription sur liste électorale\",\"description#en\":\"Pré-inscription sur liste électorale\",\"description#es\":\"Pré-inscription sur liste électorale\",\"description#it\":\"Pré-inscription sur liste électorale\",\"icon\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#bg\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#fr\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#ca\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#tr\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#en\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#es\":\"http://localhost:8000/sample-connecte-17.png\",\"icon#it\":\"http://localhost:8000/sample-connecte-17.png\",\"provider_id\":\"a2342900-f9eb-4d54-bf30-1e0d763ec4af\",\"payment_option\":\"FREE\",\"target_audience\":[\"CITIZENS\"],\"visible\":true,\"type\":\"SERVICE\",\"id\":\"4de8452c-043f-401b-882f-1a489cb22743\"},{\"name\":\"Citizen Procedures for Valence\",\"name#bg\":\"Citizen Procedures for Valence\",\"name#fr\":\"Citizen Procedures for Valence\",\"name#ca\":\"Citizen Procedures for Valence\",\"name#tr\":\"Citizen Procedures for Valence\",\"name#en\":\"Citizen Procedures for Valence\",\"name#es\":\"Citizen Procedures for Valence\",\"name#it\":\"Citizen Procedures for Valence\",\"description\":\"Citizen procedures for Valence\",\"description#bg\":\"Citizen procedures for Valence\",\"description#fr\":\"Citizen procedures for Valence\",\"description#ca\":\"Citizen procedures for Valence\",\"description#tr\":\"Citizen procedures for Valence\",\"description#en\":\"Citizen procedures for Valence\",\"description#es\":\"Citizen procedures for Valence\",\"description#it\":\"Citizen procedures for Valence\",\"icon\":\"http://www.openwide.fr\",\"icon#bg\":\"http://www.openwide.fr\",\"icon#fr\":\"http://www.openwide.fr\",\"icon#ca\":\"http://www.openwide.fr\",\"icon#tr\":\"http://www.openwide.fr\",\"icon#en\":\"http://www.openwide.fr\",\"icon#es\":\"http://www.openwide.fr\",\"icon#it\":\"http://www.openwide.fr\",\"provider_id\":\"a2342900-f9eb-4d54-bf30-1e0d763ec4af\",\"payment_option\":\"FREE\",\"target_audience\":[\"CITIZENS\"],\"visible\":true,\"type\":\"SERVICE\",\"id\":\"c4d39816-6462-4405-8571-665c7aecaebb\"},{\"name\":\"Subsidies Management\",\"name#ca\":\"Gestió de subvencions\",\"name#es\":\"Gestión de subvenciones\",\"description\":\"SM covers the whole lifcycle for subsidies and other grant management. Public bodies can manage from the preparation of the terms of reference of the call till the final grant agreement. Citizens can apply online and can monitor the evolution of its petition.\",\"description#ca\":\"GS cobreix tot el cicle de vida de les subvencions, des de l’aprovació de les bases fins la resolució final. Els ciutadans poden fer la petició en línia i controlar l’evolució del seu expedient.\",\"description#es\":\"GS cubre todo el ciclo de vida de las subvenciones, desde la aprobación de las bases hasta la resolución final. Los ciudadanos pueden hacer la petición en línea y controlar la evolución del expediente.\",\"provider_id\":\"b1eb58d5-7e1a-4088-bcd7-273629a903e8\",\"payment_option\":\"PAID\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":true,\"type\":\"APPLICATION\",\"id\":\"5AA3974F-E293-4549-98BD-47F77FB1F55E\"},{\"name\":\"Environmental Incidences Management\",\"name#ca\":\"Gestió d’incidències ambientals\",\"name#es\":\" Gestión de incidencias ambientales \",\"description\":\"EIM provides to public bodies a complete workflow for managing environmental incidences occurred in the public domain under their competence. Based on a simple mechanism of easy to fill forms and notifications, EIM allows you to deliver paperless environmental protection public services.\",\"description#ca\":\"GIA proporciona als ens públics un complert workflow per la gestió de les incidències ambientals dins el seu àmbit de competència. Basat en un mecanisme senzill de formularis i notificacions, GIA permet oferir serveis públics ambientals lliures de paper.\",\"description#es\":\"GIA proporciona a los entes públicos un completo workflow para la gestión de las incidencias ambientales dentro de su ámbito de competencia. Basado en un mecanismo sencillo de formularios y notificaciones, GIA permite ofrecer servicios públicos ambientales sin papel.\",\"provider_id\":\"b1eb58d5-7e1a-4088-bcd7-273629a903e8\",\"payment_option\":\"PAID\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":true,\"type\":\"APPLICATION\",\"id\":\"A635FB06-D698-41E3-8565-1905B680E759\"},{\"name\":\"Mapping of territorial economic activities\",\"description\":\"Mapping of territorial economic activities allows to navigate on the map in order to see the economic and productive activities in a specific portion of territory. The user can promptly identify a company clicking the point on the map, or find a firm using specific search criteria (es. name, address, ...) and view its location on the map, or identify the economic and productive activities in a given sector (code ATECO) on the selected portion of the territory.\",\"description#it\":\"Mappatura delle attività economiche sul territorio permette di navigare sulla mappa per vedere le attività economiche e produttive in una parte specifica del territorio. L'utente può immediatamente identificare una società cliccando il punto sulla mappa, o trovare una ditta usando dei criteri di ricerca (es. nome, indirizzo, ...) e visualizzarne la posizione sulla mappa, o identificare le attività economiche e produttive in un determinato settore (codice ATECO) sulla porzione del territorio selezionata.\",\"icon\":\"http://84.240.187.2/oasis/images/icon/mapactivities.jpg\",\"provider_id\":\"6b39a1cf-b9db-4ef4-b5f6-78df57d33f93\",\"payment_option\":\"FREE\",\"target_audience\":[\"PUBLIC_BODIES\"],\"visible\":true,\"type\":\"APPLICATION\",\"id\":\"55064330-bd12-4ece-b690-0487e5541899\"}]", MediaType.APPLICATION_JSON));
		List<CatalogEntry> all = catalogStore.findAllVisible(Arrays.asList(Audience.values()),
				Arrays.asList(PaymentOption.FREE, PaymentOption.PAID), new ArrayList<>(0),
                new ArrayList<>(0), new ArrayList<>(0), null, null, from);
		assertEquals(13, all.size());
		mock.verify();
	}

	@Test
	public void testFindService() throws Exception {
		String response = "{\"name\":\"Citizen Procedures for Valence\",\"name#fr\":\"Citizen Procedures for Valence\",\"name#bg\":\"Процедури гражданин Valence\",\"name#tr\":\"Citizen Procedures for Valence\",\"name#ca\":\"Citizen Procedures for Valence\",\"name#en\":\"Citizen Procedures for Valence\",\"name#es\":\"Citizen Procedures for Valence\",\"name#it\":\"Citizen Procedures for Valence\",\"description\":\"Citizen procedures for Valence\",\"description#fr\":\"Citizen procedures for Valence\",\"description#bg\":\"Citizen procedures for Valence\",\"description#tr\":\"Citizen procedures for Valence\",\"description#ca\":\"Citizen procedures for Valence\",\"description#en\":\"Citizen procedures for Valence\",\"description#es\":\"Citizen procedures for Valence\",\"description#it\":\"Citizen procedures for Valence\",\"icon\":\"http://www.openwide.fr\",\"icon#fr\":\"http://www.openwide.fr\",\"icon#bg\":\"http://www.openwide.fr\",\"icon#tr\":\"http://www.openwide.fr\",\"icon#ca\":\"http://www.openwide.fr\",\"icon#en\":\"http://www.openwide.fr\",\"icon#es\":\"http://www.openwide.fr\",\"icon#it\":\"http://www.openwide.fr\",\"provider_id\":\"a2342900-f9eb-4d54-bf30-1e0d763ec4af\",\"payment_option\":\"FREE\",\"target_audience\":[\"CITIZENS\", \"COMPANIES\"],\"visible\":true,\"visibility\":\"VISIBLE\",\"restricted\":false,\"access_control\":\"ANYONE\",\"local_id\":\"front\",\"instance_id\":\"517fda66-0371-4c19-8698-359ca32f2bf2\",\"service_uri\":\"http://localhost:9090/front/valence\",\"notification_uri\":\"http://localhost:9090/front/valence/notifications\",\"redirect_uris\":[\"http://localhost:9090/front/callback\"],\"post_logout_redirect_uris\":[\"http://localhost:9090/front\"],\"territory_id\":\"26000\",\"type\":\"SERVICE\",\"id\":\"c4d39816-6462-4405-8571-665c7aecaebb\",\"modified\":1409738662341}";

		MockRestServiceServer mock = MockRestServiceServer.createServer(kernelRestTemplate);

		mock.expect(requestTo("http://localhost:8081/apps/service/c4d39816-6462-4405-8571-665c7aecaebb"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

		ServiceEntry entry = catalogStore.findService("c4d39816-6462-4405-8571-665c7aecaebb");
		assertEquals(PaymentOption.FREE, entry.getPaymentOption());
		assertEquals("Citizen Procedures for Valence", entry.getName());
		assertEquals("Процедури гражданин Valence", entry.getName(Locale.forLanguageTag("bg")));
		assertEquals(2, entry.getTargetAudience().size());
		assertEquals("SERVICE", entry.getType().name());
		assertEquals("VISIBLE", entry.getVisibility());
		assertEquals("ANYONE", entry.getAccessControl());
		mock.verify();

		mock.reset();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setETag("\"1234\"");
        mock.expect(requestTo("http://localhost:8081/apps/service/c4d39816-6462-4405-8571-665c7aecaebb"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON).headers(httpHeaders));

        mock.expect(requestTo("http://localhost:8081/apps/service/c4d39816-6462-4405-8571-665c7aecaebb"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.visibility").exists())
                .andExpect(jsonPath("$.visibility").value("HIDDEN"))
                .andExpect(jsonPath("$.visible").doesNotExist())
                // we don't care of the response body but we need one
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

		entry.setVisibility("HIDDEN");
		catalogStore.updateService(entry.getId(), entry);

		mock.verify();
	}
}
