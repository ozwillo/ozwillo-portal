package org.oasis_eu.portal.core.services.sitemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.mongo.dao.sitemap.SiteMapHeaderRepository;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapMenuItem;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapMenuSet;
import org.oasis_eu.portal.core.services.sitemap.xml.HeaderMenuSet;
import org.oasis_eu.portal.OasisPortal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.Resource;
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
@SpringApplicationConfiguration(classes = {OasisPortal.class, MockServletContext.class})
public class SiteMapHeaderParserTest {

	@Autowired
	@Qualifier("xmlAwareRestTemplate")
	private RestTemplate restTemplate;

	@Value("${web.sitemap.url_header}")
	private String url_header;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private SiteMapHeaderRepository repository;

	@Autowired
	private SiteMapService siteMapService;

	@Autowired
	private SiteMapUpdater siteMapUpdater;

	@Test
	public void testParseHeaderSiteMap() throws Exception {

		InputStream stream = getClass().getClassLoader().getResourceAsStream("xml/header.xml");
		XmlMapper xmlMapper = new XmlMapper();
		HeaderMenuSet foo = xmlMapper.readValue(stream, HeaderMenuSet.class);

		validateMenuSet(foo);

		List<SiteMapMenuItem> siteMapMenuItems = foo.getMenuset().get(0).getItems();
		assertEquals("/static/img/logo.png", siteMapMenuItems.get(0).getImgUrl());
		assertEquals("https://portal.ozwillo.com/fr/store", siteMapMenuItems.get(4).getUrl());
		assertEquals("/static/img/icone-catalogue-color.png", siteMapMenuItems.get(4).getImgUrl());
	}

	@Test
	@DirtiesContext
	public void testLoadRemote() throws Exception {

		Resource resource = resourceLoader.getResource("classpath:/xml/header.xml");
		MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
		server.expect(requestTo(url_header)).andRespond(withSuccess(resource, MediaType.APPLICATION_XML));

		HeaderMenuSet foo = restTemplate.getForObject(url_header, HeaderMenuSet.class);

		validateMenuSet(foo);

		List<SiteMapMenuItem> siteMapMenuItems = foo.getMenuset().get(0).getItems();
		assertEquals("/static/img/logo.png", siteMapMenuItems.get(0).getImgUrl());
		assertEquals("https://portal.ozwillo.com/fr/store", siteMapMenuItems.get(4).getUrl());
		assertEquals("/static/img/icone-catalogue-color.png", siteMapMenuItems.get(4).getImgUrl());

		server.verify();
	}

	@Test
	@DirtiesContext
	public void testUpdateSiteMap() throws Exception {

		Resource resource = resourceLoader.getResource("classpath:/xml/header.xml");
		MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
		server.expect(requestTo(url_header)).andRespond(withSuccess(resource, MediaType.APPLICATION_XML));

		repository.deleteAll();

		siteMapUpdater.reloadHeader();

		SiteMapMenuSet sitemapHeaderFR = siteMapService.getSiteMapHeader("fr");
		validateFRData(sitemapHeaderFR);

		SiteMapMenuSet sitemapHeaderEN = siteMapService.getSiteMapHeader("en");
		validateENData(sitemapHeaderEN);

		server.verify();
	}

	// utility methods
	private void validateFRData(SiteMapMenuSet sitemapHeader){
		assertNotNull(sitemapHeader);
		assertNotNull(sitemapHeader.getItems().get(0));

		assertEquals("fr", sitemapHeader.getLanguage());
		assertEquals(8, sitemapHeader.getItems().size());
		assertEquals(3, sitemapHeader.getContentItems().size());
		assertEquals("menu", sitemapHeader.getItems().get(3).getType());
		assertEquals("Actualités", sitemapHeader.getItems().get(3).getLabel());

		SiteMapMenuItem offersMenuItem = sitemapHeader.getItems().get(2);
		assertEquals("Offres", offersMenuItem.getLabel());
		assertEquals("submenu", offersMenuItem.getType());
		assertEquals(3, offersMenuItem.getItems().size());
		assertEquals("Portail", offersMenuItem.getItems().get(1).getLabel());
	}

	private void validateENData(SiteMapMenuSet sitemapHeader){
		assertNotNull(sitemapHeader);
		assertNotNull(sitemapHeader.getItems().get(0));

		assertEquals("en", sitemapHeader.getLanguage());
		assertEquals(8, sitemapHeader.getItems().size());
		assertEquals(3, sitemapHeader.getContentItems().size());

		SiteMapMenuItem catalogMenuItem = sitemapHeader.getItems().get(4);
		assertEquals("https://portal.ozwillo.com/en/store", catalogMenuItem.getUrl());
		assertEquals("catalog", catalogMenuItem.getType());
		assertEquals("Catalog", catalogMenuItem.getLabel());
	}

	private void validateMenuSet(HeaderMenuSet foo){

		assertEquals(2, foo.getMenuset().size());

		//French
		SiteMapMenuSet sitemapHeaderFR = foo.getMenuset().get(0);
		validateFRData(sitemapHeaderFR);

		// English
		SiteMapMenuSet sitemapHeaderEN = foo.getMenuset().get(1);
		validateENData(sitemapHeaderEN);
	}
}
