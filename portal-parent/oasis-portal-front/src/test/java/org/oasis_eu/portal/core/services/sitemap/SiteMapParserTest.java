package org.oasis_eu.portal.core.services.sitemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.mongo.dao.sitemap.SiteMapRepository;
import org.oasis_eu.portal.core.services.sitemap.xml.Footer;
import org.oasis_eu.portal.main.OasisPortal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
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
public class SiteMapParserTest {

    @Test
    public void testParseSiteMap() throws Exception {

        InputStream stream = getClass().getClassLoader().getResourceAsStream("xml/footer.xml");
        XmlMapper xmlMapper = new XmlMapper();
        Footer foo = xmlMapper.readValue(stream, Footer.class);
        assertEquals(4, foo.getMenuset().size());
        assertEquals("fr", foo.getMenuset().get(0).getLanguage());
        assertEquals(12, foo.getMenuset().get(0).getEntries().size());
        assertEquals("/fr/decouvrir", foo.getMenuset().get(0).getEntries().get(0).getUrl());
        assertEquals("Découvrir", foo.getMenuset().get(0).getEntries().get(0).getLabel());

        assertEquals("en", foo.getMenuset().get(1).getLanguage());
        assertEquals(12, foo.getMenuset().get(1).getEntries().size());
        assertNull(foo.getMenuset().get(1).getEntries().get(7));
    }

    @Autowired
    @Qualifier("xmlAwareRestTemplate")
    private RestTemplate restTemplate;

    @Value("${web.sitemap.url_footer}")
    private String sitemapUrl;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @DirtiesContext
    public void testLoadRemote() throws Exception {
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(sitemapUrl)).andRespond(withSuccess(resourceLoader.getResource("classpath:/xml/footer.xml"), MediaType.APPLICATION_XML));

        Footer foo = restTemplate.getForObject(sitemapUrl, Footer.class);
        assertEquals(4, foo.getMenuset().size());
        assertEquals("fr", foo.getMenuset().get(0).getLanguage());
        assertEquals(12, foo.getMenuset().get(0).getEntries().size());
        assertEquals("/fr/decouvrir", foo.getMenuset().get(0).getEntries().get(0).getUrl());
        assertEquals("Découvrir", foo.getMenuset().get(0).getEntries().get(0).getLabel());

        assertEquals("en", foo.getMenuset().get(1).getLanguage());
        assertEquals(12, foo.getMenuset().get(1).getEntries().size());
        assertNull(foo.getMenuset().get(1).getEntries().get(7));

        server.verify();
    }


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
    @DirtiesContext
    public void testUpdateSiteMap() throws Exception {

        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(sitemapUrl)).andRespond(withSuccess(resourceLoader.getResource("classpath:/xml/footer.xml"), MediaType.APPLICATION_XML));

        this.clean();

        siteMapUpdater.reloadFooter();

        assertNotNull(siteMapService.getSiteMapFooter("fr"));
        assertEquals("http://www.ozwillo-dev.eu/fr/decouvrir", siteMapService.getSiteMapFooter("fr").get(0).getUrl());
        assertNotNull(siteMapService.getSiteMapFooter("en").get(7));
        assertEquals("", siteMapService.getSiteMapFooter("en").get(7).getLabel());
        assertEquals("", siteMapService.getSiteMapFooter("en").get(7).getUrl());


        server.verify();
    }
}
