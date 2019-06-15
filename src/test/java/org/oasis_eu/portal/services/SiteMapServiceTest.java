package org.oasis_eu.portal.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.oasis_eu.portal.config.environnements.EnvProperties;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.oasis_eu.portal.config.environnements.helpers.SiteMapEnv;
import org.oasis_eu.portal.config.environnements.helpers.WebEnv;
import org.oasis_eu.portal.dao.SiteMapComponentsRepository;
import org.oasis_eu.portal.dao.StylePropertiesMapRepository;
import org.oasis_eu.portal.model.sitemap.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = SiteMapService.class)
@ActiveProfiles("test")
public class SiteMapServiceTest {

	@MockBean
	private SiteMapComponentsRepository siteMapComponentsRepository;

	@MockBean
	private StylePropertiesMapRepository stylePropertiesMapRepository;

	@Autowired
	private SiteMapService siteMapService;

	@MockBean
	private EnvPropertiesService envPropertiesService;

	@MockBean
	private EnvProperties envProperties;

	@BeforeEach
	public void init() {
		EnvConfig envConfig = new EnvConfig();
		envConfig.setBaseImageUrl("http://localhost:3000/media");
		envConfig.setBaseUrl("http://localhost:3000");
		WebEnv webEnv = new WebEnv();
		webEnv.setHome("http://localhost:8089");
		SiteMapEnv siteMapEnv = new SiteMapEnv();
		siteMapEnv.setUrl_footer("http://localhost:8089/footer.json");
		webEnv.setSitemap(siteMapEnv);
		envConfig.setWeb(webEnv);
		when(envPropertiesService.getCurrentConfig())
				.thenReturn(envConfig);

		when(envProperties.getConfs())
				.thenReturn(Collections.singletonMap("ozwillo", envConfig));
	}

	@Test
	public void siteMapFooterShouldBeFound() {
		when(siteMapComponentsRepository.findByWebsite("ozwillo"))
				.thenReturn(Optional.of(siteMapComponents()));

		List<SiteMapEntry> entries = siteMapService.getSiteMapFooter("ozwillo", "fr");

		Mockito.verify(siteMapComponentsRepository).findByWebsite(eq("ozwillo"));

		assertEquals(2, entries.size());
		assertTrue(entries.stream().anyMatch(siteMapEntry -> siteMapEntry.getUrl().equals("http://localhost:8089/entry1")));
		assertTrue(entries.stream().anyMatch(siteMapEntry -> siteMapEntry.getLabel().equals("entry2")));
		assertTrue(entries.stream().allMatch(siteMapEntry -> siteMapEntry.getRow() == 0));
	}

	@Test
	public void siteMapFooterShouldNotBeFoundForNonExistentWebsite() {
		when(siteMapComponentsRepository.findByWebsite("nonExistentWebsite"))
				.thenReturn(Optional.empty());

		List<SiteMapEntry> entries = siteMapService.getSiteMapFooter("nonExistentWebsite", "fr");

		assertTrue(entries.isEmpty());
	}

	@Test
	public void siteMapFooterShouldNotBeFoundForUnknownLang() {
		when(siteMapComponentsRepository.findByWebsite("ozwillo"))
				.thenReturn(Optional.of(siteMapComponents()));

		List<SiteMapEntry> entries = siteMapService.getSiteMapFooter("ozwillo", "en");

		assertTrue(entries.isEmpty());
	}

	@Test
	public void shouldParseFooterData() throws IOException {

		WireMockServer wireMockServer = new WireMockServer(8089);
		wireMockServer.start();

		when(siteMapComponentsRepository.findByWebsite("ozwillo"))
				.thenReturn(Optional.empty());

		ClassPathResource classPathResource = new ClassPathResource("/json/footer.json");
		String body = StreamUtils.copyToString(classPathResource.getInputStream(), Charset.forName("UTF-8"));
		wireMockServer.stubFor(get(urlPathEqualTo("/footer.json"))
				.willReturn(okJson(body)));

		siteMapService.reloadFooter();

		Mockito.verify(siteMapComponentsRepository).findByWebsite("ozwillo");
		Mockito.verify(siteMapComponentsRepository).save(ArgumentMatchers.argThat(siteMapComponents ->
			siteMapComponents.getWebsite().equals("ozwillo") &&
					siteMapComponents.getSiteMapMenuFooter().size() == 7 &&
					siteMapComponents.getSiteMapMenuFooter().get(0).getEntries().size() == 14 &&
					siteMapComponents.getSiteMapMenuFooter().stream().anyMatch(siteMapMenuFooter -> siteMapMenuFooter.getLanguage().equals("fr"))
		));

		wireMockServer.verify(getRequestedFor(urlEqualTo("/footer.json")));
		wireMockServer.stop();
	}

	private SiteMapComponents siteMapComponents() {
		SiteMapComponents siteMapComponents = new SiteMapComponents();
		siteMapComponents.setWebsite("ozwillo");

		SiteMapMenuFooter siteMapMenuFooter = new SiteMapMenuFooter();
		siteMapMenuFooter.setLanguage("fr");
		SiteMapEntry siteMapEntry1 = new SiteMapEntry("/entry1", 0, "entry1");
		SiteMapEntry siteMapEntry2 = new SiteMapEntry("/entry2", 0, "entry2");
		siteMapMenuFooter.setEntries(Arrays.asList(siteMapEntry1, siteMapEntry2));

		siteMapComponents.setSiteMapMenuFooter(Collections.singletonList(siteMapMenuFooter));

		return siteMapComponents;
	}
}
