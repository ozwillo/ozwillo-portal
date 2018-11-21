package org.oasis_eu.portal.services.sitemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.dao.SiteMapComponentsRepository;
import org.oasis_eu.portal.model.sitemap.FooterMenuSet;
import org.oasis_eu.portal.model.sitemap.SiteMapEntry;
import org.oasis_eu.portal.model.sitemap.SiteMapMenuFooter;
import org.oasis_eu.portal.services.SiteMapService;
import org.oasis_eu.portal.services.jobs.SiteMapUpdater;
import org.oasis_eu.portal.model.sitemap.Footer;
import org.oasis_eu.portal.OzwilloPortal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;


/**
 * User: schambon
 * Date: 1/8/15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = {OzwilloPortal.class, MockServletContext.class})
public class SiteMapFooterParserTest {

	@Value("${confs.ozwillo.web.sitemap.url_footer}")
	private String sitemapUrl;

	@Autowired
	private SiteMapComponentsRepository siteMapComponentsRepository;

	@Autowired
	private SiteMapService siteMapService;

	@Autowired
	private SiteMapUpdater siteMapUpdater;

	@Before
	public void init() {
		siteMapUpdater.initializeSiteMapComponents();
    }

    @After
	public void clean() {
		siteMapComponentsRepository.deleteAll();
	}

	@Test
	@DirtiesContext
	public void testLoadRemote() {
	    RestTemplate restTemplate = new RestTemplate();
		Footer footer = restTemplate.getForObject(sitemapUrl, FooterMenuSet.class).getFooter();

		assertEquals(7, footer.getMenuset().size());
		SiteMapMenuFooter frSiteMapMenuFooter = footer.getMenuset().get(0);

		assertEquals("fr", frSiteMapMenuFooter.getLanguage());
		assertEquals(14, frSiteMapMenuFooter.getEntries().size());
		assertEquals("/fr/association", frSiteMapMenuFooter.getEntries().get(0).getUrl());
		assertEquals("Association", frSiteMapMenuFooter.getEntries().get(0).getLabel());

		SiteMapMenuFooter enSiteMapMenuFooter = footer.getMenuset().get(1);
		assertEquals("en", enSiteMapMenuFooter.getLanguage());
		assertEquals(14, enSiteMapMenuFooter.getEntries().size());
    }

	@Test
	@DirtiesContext
	public void testUpdateSiteMap()  {
		siteMapUpdater.reloadFooter();

        List<SiteMapEntry> siteMapEN = siteMapService.getSiteMapFooter("ozwillo","en");
		List<SiteMapEntry> siteMapFR = siteMapService.getSiteMapFooter("ozwillo","fr");

		assertNotNull(siteMapFR);
		assertEquals("https://www.ozwillo-dev.eu/fr/association", siteMapFR.get(0).getUrl());

		assertNotNull(siteMapEN);
		assertEquals("Genesis", siteMapEN.get(7).getLabel());
		assertEquals("https://www.ozwillo-dev.eu/en/genesis", siteMapEN.get(7).getUrl());

    }
}
