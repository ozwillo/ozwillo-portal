package org.oasis_eu.portal.core.services.sitemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.mongo.dao.sitemap.SiteMapHeaderRepository;
import org.oasis_eu.portal.core.mongo.model.sitemap.SiteMapMenuSet;
import org.oasis_eu.portal.core.services.sitemap.xml.HeaderMenuSet;
import org.oasis_eu.portal.main.OasisPortal;
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
        assertEquals("/images/ui/apps-on.png", foo.getMenuset().get(0).getItems().get(0).getImg_url());
        assertEquals("/fr/oz/decouvrir", foo.getMenuset().get(0).getSubmenus().get(0).getUrl());

    }


    @Test
    @DirtiesContext
    public void testLoadRemote() throws Exception {

        Resource resource = resourceLoader.getResource("classpath:/xml/header.xml");
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(url_header)).andRespond(withSuccess(resource, MediaType.APPLICATION_XML));

        HeaderMenuSet foo = restTemplate.getForObject(url_header, HeaderMenuSet.class);

        validateMenuSet(foo);
        assertEquals("/images/ui/apps-on.png", foo.getMenuset().get(0).getItems().get(0).getImg_url());
        assertEquals("/fr/oz/decouvrir", foo.getMenuset().get(0).getSubmenus().get(0).getUrl());

        server.verify();
    }


    @Test
    @DirtiesContext
    public void testUpdateSiteMap() throws Exception {

        Resource resource = resourceLoader.getResource("classpath:/xml/header.xml");
        //Resource resource = resourceLoader.getResource("http://tempsend.com/855AFC2863/1C03/header.xml");
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(url_header)).andRespond(withSuccess(resource, MediaType.APPLICATION_XML));

        repository.deleteAll();

        siteMapUpdater.reloadHeader();

        SiteMapMenuSet sitemapHeaderFR = siteMapService.getSiteMapHeader("fr");
        validateFRData(sitemapHeaderFR);
        assertEquals("http://www.ozwillo-dev.eu/images/ui/apps-on.png", sitemapHeaderFR.getItems().get(0).getImg_url());
        assertEquals("http://www.ozwillo-dev.eu/fr/oz/decouvrir", sitemapHeaderFR.getSubmenus().get(0).getSubItems().get(0).getUrl());
        assertEquals("http://www.ozwillo-dev.eu/fr/oz/decouvrir", sitemapHeaderFR.getSubmenus().get(0).getUrl());

        SiteMapMenuSet sitemapHeaderEN = siteMapService.getSiteMapHeader("en");
        validateENData(sitemapHeaderEN);
        assertEquals("http://www.ozwillo-dev.eu/images/ui/apps-on.png", sitemapHeaderEN.getItems().get(0).getImg_url());
        assertEquals("http://www.ozwillo-dev.eu/en/oz/discover", sitemapHeaderEN.getSubmenus().get(0).getSubItems().get(0).getUrl());
        assertEquals("http://www.ozwillo-dev.eu/en/oz/discover", sitemapHeaderEN.getSubmenus().get(0).getUrl());

        server.verify();
    }

    // utility methods
    private void validateFRData(SiteMapMenuSet sitemapHeader){
        assertNotNull(sitemapHeader);
        assertNotNull(sitemapHeader.getItems().get(0));
        assertNotNull(sitemapHeader.getSubmenus());
        assertEquals("fr", sitemapHeader.getLanguage());
        assertEquals(5, sitemapHeader.getItems().size());
        assertEquals("catalog", sitemapHeader.getItems().get(1).getType());
        assertEquals("Catalogue", sitemapHeader.getItems().get(1).getLabel());

        assertNotNull(sitemapHeader.getSubmenus());
        assertEquals("Découvrir", sitemapHeader.getSubmenus().get(0).getTitle());
        assertEquals("Découvrir la plate-forme", sitemapHeader.getSubmenus().get(0).getSubItems().get(0).getLabel());
    }

    private void validateENData(SiteMapMenuSet sitemapHeader){
        assertNotNull(sitemapHeader);
        assertNotNull(sitemapHeader.getItems().get(0));
        assertNotNull(sitemapHeader.getSubmenus().get(0));     

        assertEquals("en", sitemapHeader.getLanguage());
        assertEquals(5, sitemapHeader.getItems().size());
        assertEquals("", sitemapHeader.getItems().get(4).getLabel());
        assertEquals("https://portal.ozwillo.com/en/store", sitemapHeader.getItems().get(1).getUrl());
        assertEquals("catalog", sitemapHeader.getItems().get(1).getType());
        assertEquals("Catalog", sitemapHeader.getItems().get(1).getLabel());

        assertNotNull(sitemapHeader.getSubmenus());
        assertEquals("Discover", sitemapHeader.getSubmenus().get(0).getTitle());
        assertEquals("Discovering the Platform", sitemapHeader.getSubmenus().get(0).getSubItems().get(0).getLabel());

    }

    private void validateMenuSet(HeaderMenuSet foo){

        assertEquals(7, foo.getMenuset().size());

        //French
        SiteMapMenuSet sitemapHeaderFR = foo.getMenuset().get(0);
        validateFRData(sitemapHeaderFR);

        // English
        SiteMapMenuSet sitemapHeaderEN = foo.getMenuset().get(1);
        validateENData(sitemapHeaderEN);

    }

}
