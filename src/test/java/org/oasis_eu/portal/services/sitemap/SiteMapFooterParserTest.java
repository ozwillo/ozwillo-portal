package org.oasis_eu.portal.services.sitemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.dao.portal.sitemap.SiteMapRepository;
import org.oasis_eu.portal.model.sitemap.SiteMap;
import org.oasis_eu.portal.services.sitemap.xml.Footer;
import org.oasis_eu.portal.OzwilloPortal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * User: schambon
 * Date: 1/8/15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = {OzwilloPortal.class, MockServletContext.class})
public class SiteMapFooterParserTest {

	@Autowired
	@Qualifier("xmlAwareRestTemplate")
	private RestTemplate restTemplate;

	@Value("${web.sitemap.url_footer}")
	private String sitemapUrl;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private SiteMapRepository repository;

	@Autowired
	private SiteMapService siteMapService;

	@Autowired
	private SiteMapUpdater siteMapUpdater;

	@Before
	public void clean() {
		repository.deleteAll();
	}

	@Test
	public void testParseSiteMap() throws Exception {

		InputStream stream = getClass().getClassLoader().getResourceAsStream("xml/footer.xml");
		XmlMapper xmlMapper = new XmlMapper();
		Footer footer = xmlMapper.readValue(stream, Footer.class);
		assertEquals(7, footer.getMenuset().size());
		SiteMap frSiteMap = footer.getMenuset().get(0);
		assertEquals("fr", frSiteMap.getLanguage());
		assertEquals(13, frSiteMap.getEntries().size());
		assertEquals("/fr/association", frSiteMap.getEntries().get(0).getUrl());
		assertEquals("Association", frSiteMap.getEntries().get(0).getLabel());

		SiteMap enSiteMap = footer.getMenuset().get(1);
		assertEquals("en", enSiteMap.getLanguage());
		assertEquals(13, enSiteMap.getEntries().size());
	}

	@Test
	@DirtiesContext
	public void testLoadRemote() throws Exception {
		MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
		server.expect(requestTo(sitemapUrl)).andRespond(withSuccess(resourceLoader.getResource("classpath:/xml/footer.xml"), MediaType.APPLICATION_XML));

		Footer footer = restTemplate.getForObject(sitemapUrl, Footer.class);
		assertEquals(7, footer.getMenuset().size());
		SiteMap frSiteMap = footer.getMenuset().get(0);
		assertEquals("fr", frSiteMap.getLanguage());
		assertEquals(13, frSiteMap.getEntries().size());
		assertEquals("/fr/association", frSiteMap.getEntries().get(0).getUrl());
		assertEquals("Association", frSiteMap.getEntries().get(0).getLabel());

		SiteMap enSiteMap = footer.getMenuset().get(1);
		assertEquals("en", enSiteMap.getLanguage());
		assertEquals(13, enSiteMap.getEntries().size());

		server.verify();
	}

	@Test
	@DirtiesContext
	public void testUpdateSiteMap() throws Exception {

		MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
		server.expect(requestTo(sitemapUrl)).andRespond(withSuccess(resourceLoader.getResource("classpath:/xml/footer.xml"), MediaType.APPLICATION_XML));

		this.clean();

		siteMapUpdater.reloadFooter();

		assertNotNull(siteMapService.getSiteMapFooter("fr"));
		assertEquals("http://www.ozwillo-dev.eu/fr/association", siteMapService.getSiteMapFooter("fr").get(0).getUrl());
		assertNotNull(siteMapService.getSiteMapFooter("en").get(7));
		assertEquals("Genesis", siteMapService.getSiteMapFooter("en").get(7).getLabel());
		assertEquals("http://www.ozwillo-dev.eu/en/genesis", siteMapService.getSiteMapFooter("en").get(7).getUrl());

		server.verify();
	}
}
