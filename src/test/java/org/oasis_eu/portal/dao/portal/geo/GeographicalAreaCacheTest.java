package org.oasis_eu.portal.dao.portal.geo;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.dao.GeographicalAreaCache;
import org.oasis_eu.portal.model.geo.GeographicalArea;
import org.oasis_eu.portal.model.geo.GeographicalAreaReplicationStatus;
import org.oasis_eu.portal.model.search.Tokenizer;
import org.oasis_eu.portal.OzwilloPortal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = {OzwilloPortal.class, MockServletContext.class})
public class GeographicalAreaCacheTest {

	@Autowired
	private GeographicalAreaCache cache;

	@Autowired
	private MongoTemplate template;

	@Autowired
	private Tokenizer tokenizer;

	@Test
	public void testSearch() throws Exception {

		template.remove(new Query(), GeographicalArea.class);

		cache.save(area("ville1", "fr", "uri1"));
		cache.save(area("ville2", "fr", "uri1"));
		cache.save(area("ville3", "fr", "uri1"));
		cache.save(area("ville4", "fr", "uri2"));
		cache.save(area("ville5", "fr", "uri3"));
		cache.save(area("ville6", "fr", "uri4"));

		assertEquals(6, template.findAll(GeographicalArea.class).size());
		List<GeographicalArea> areas = cache.search(null, null, "fr", "ville", 0, 10).collect(Collectors.toList());
		assertEquals(4, areas.size());
		assertEquals(GeographicalAreaReplicationStatus.INCOMING, areas.get(0).getStatus());
	}

	@Test
	public void testSearch2() throws Exception {
		template.remove(new Query(), GeographicalArea.class);

		cache.save(area("ville1", "fr", "uri1"));
		cache.save(area("ville1", "en", "uri2"));
		cache.save(area("ville2", "fr", "uri3"));

		assertEquals(2, cache.search(null, null, "fr", "vil", 0, 10).count());
		assertEquals(1, cache.search(null, null, "en", "vil", 0, 10).count());
	}

	@Test
	public void testSearch3() throws Exception {
		template.remove(new Query(), GeographicalArea.class);

		cache.save(area("VILLE", "fr", "uri1"));
		cache.save(area("ville", "fr", "uri2"));

		assertEquals(2, cache.search(null, null, "fr", "vil", 0, 10).count());
	}

	@Test
	public void testSwitchToOnline() throws Exception {
		template.remove(new Query(), GeographicalArea.class);

		cache.save(area("ville1", "fr", "uri1"));
		cache.save(area("ville2", "fr", "uri2"));
		cache.save(area("ville3", "fr", "uri3"));
		cache.save(area("ville1", "fr", "uri1"));

		assertEquals(3, cache.search(null, null, "fr", "vil", 0, 10).count());
		cache.switchToOnline();
		assertEquals(3, cache.search(null, null, "fr", "vil", 0, 10).count());
		assertEquals(GeographicalAreaReplicationStatus.ONLINE, cache.search(null, null, "fr", "vil", 0, 10).findFirst().get().getStatus());

		cache.save(area("villemiseàjour", "fr", "uri1"));
		// although we've updated "uri1", the old online version is still there and that's the one we should find
		GeographicalArea found = cache.search(null, null, "fr", "vil", 0, 10).filter(area -> area.getUri().equals("uri1")).findAny().get();
		assertEquals("ville1", found.getName());
		assertEquals(GeographicalAreaReplicationStatus.ONLINE, found.getStatus());

		cache.deleteByStatus(GeographicalAreaReplicationStatus.ONLINE);
		cache.switchToOnline();

		assertEquals(1, cache.search(null, null, "fr", "vil", 0, 10).count());
		assertEquals("villemiseàjour", cache.search(null, null, "fr", "vil", 0, 10).findAny().get().getName());
	}

	@Test
	public void withTokenization() {
		template.remove(new Query(), GeographicalArea.class);

		cache.save(area("Éragny-Sur-Epte", "fr", "eragny"));
		cache.save(area("Ollourdes", "fr", "ollourdes"));
		cache.save(area("Saint-Genis-Laval", "fr", "laval"));
		cache.save(area("Lyon Saint-Éxupéry", "fr", "saint-ex"));
		cache.save(area("Saint-Genis-Les-Ollières", "fr", "ollieres"));
		cache.save(area("Genisette-Lès-Bain", "fr", "bains"));


		List<GeographicalArea> genis = cache.search(null, null, "fr", "gen sai", 0, 10).collect(Collectors.toList());

		// based on this we should get two results
		assertEquals(2, genis.size());

		// searching for "era" and "éra" does yield "éragny"
		assertEquals("eragny", cache.search(null, null, "fr", "era", 0, 10).findFirst().get().getUri());
		assertEquals("eragny", cache.search(null, null, "fr", "éra", 0, 10).findFirst().get().getUri());

		// with tiny tokens...
		assertEquals(0, cache.search(null, null, "fr", "ge ol er", 0, 10).count());
	}

	private GeographicalArea area(String name, String lang, String uri) {
		GeographicalArea area = new GeographicalArea();
		area.setLang(lang);
		area.setName(name);
		area.setUri(uri);
		area.setNameTokens(tokenizer.tokenize(name));
		area.setReplicationTime(Instant.now());
		return area;
	}
}
