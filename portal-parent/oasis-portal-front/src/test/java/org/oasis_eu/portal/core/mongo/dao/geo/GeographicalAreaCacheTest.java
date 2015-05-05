package org.oasis_eu.portal.core.mongo.dao.geo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalArea;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalAreaReplicationStatus;
import org.oasis_eu.portal.main.OasisPortal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = {OasisPortal.class, MockServletContext.class})
public class GeographicalAreaCacheTest {

    @Autowired
    private GeographicalAreaCache cache;

    @Autowired
    private MongoTemplate template;

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
        List<GeographicalArea> areas = cache.search("fr", "ville", 0, 10).collect(Collectors.toList());
        assertEquals(4, areas.size());
        assertEquals(GeographicalAreaReplicationStatus.INCOMING, areas.get(0).getStatus());
    }

    @Test
    public void testSearch2() throws Exception {
        template.remove(new Query(), GeographicalArea.class);

        cache.save(area("ville1", "fr", "uri1"));
        cache.save(area("ville1", "en", "uri2"));
        cache.save(area("ville2", "fr", "uri3"));

        assertEquals(2, cache.search("fr", "vil", 0, 10).count());
        assertEquals(1, cache.search("en", "vil", 0, 10).count());
    }

    @Test
    public void testSearch3() throws Exception {
        template.remove(new Query(), GeographicalArea.class);

        cache.save(area("VILLE", "fr", "uri1"));
        cache.save(area("ville", "fr", "uri2"));

        assertEquals(2, cache.search("fr", "vil", 0, 10).count());
    }

    @Test
    public void testSwitchToOnline() throws Exception {
        template.remove(new Query(), GeographicalArea.class);

        cache.save(area("ville1", "fr", "uri1"));
        cache.save(area("ville2", "fr", "uri2"));
        cache.save(area("ville3", "fr", "uri3"));
        cache.save(area("ville1", "fr", "uri1"));

        assertEquals(3, cache.search("fr", "vil", 0, 10).count());
        cache.switchToOnline();
        assertEquals(3, cache.search("fr", "vil", 0, 10).count());
        assertEquals(GeographicalAreaReplicationStatus.ONLINE, cache.search("fr", "vil", 0, 10).findFirst().get().getStatus());

        cache.save(area("villemiseàjour", "fr", "uri1"));
        GeographicalArea found = cache.search("fr", "vil", 0, 10).filter(area -> area.getUri().equals("uri1")).findAny().get();
        assertEquals("villemiseàjour", found.getName());
        assertEquals(GeographicalAreaReplicationStatus.INCOMING, found.getStatus());

        cache.deleteByStatus(GeographicalAreaReplicationStatus.ONLINE);
        cache.switchToOnline();

        assertEquals(1, cache.search("fr", "vil", 0, 10).count());
        assertEquals("villemiseàjour", cache.search("fr", "vil", 0, 10).findAny().get().getName());
    }

    private GeographicalArea area(String name, String lang, String uri) {
        GeographicalArea area = new GeographicalArea();
        area.setLang(lang);
        area.setName(name);
        area.setUri(uri);
        return area;
    }
}