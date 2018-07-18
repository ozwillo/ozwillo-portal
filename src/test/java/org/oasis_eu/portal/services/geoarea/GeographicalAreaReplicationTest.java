package org.oasis_eu.portal.services.geoarea;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.dao.portal.geo.GeographicalAreaCache;
import org.oasis_eu.portal.model.geo.GeographicalArea;
import org.oasis_eu.portal.OzwilloPortal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotEquals;


/**
 * Its output can also be used to fill non-test portal instance cache, this way :
 * mongodump -d portal_test -c geographical_area -o portal_geoarea1
 * mongorestore -d portal -c geographical_area portal_geoarea1/portal_test/geographical_area.bson
 * 
 * @author schambon
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = {OzwilloPortal.class}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class GeographicalAreaReplicationTest {

	@Autowired
	private GeographicalAreaCache cache;

	@Autowired
	private MongoTemplate template;

	@Value("${application.geoarea.replication_test:false}")
	private boolean testReplication = false;

	@Test
	public void testReplicate() throws Exception {
		if(!testReplication) return; // skip replication testing since it is VERY long

		template.remove(new Query(), GeographicalArea.class);

		cache.replicate();

		assertNotEquals(template.find(new Query(), GeographicalArea.class).size(), 0);
	}
}
