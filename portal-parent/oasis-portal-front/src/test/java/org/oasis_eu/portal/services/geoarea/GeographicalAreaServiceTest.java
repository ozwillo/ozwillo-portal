package org.oasis_eu.portal.services.geoarea;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.mongo.model.geo.GeographicalArea;
import org.oasis_eu.portal.main.OasisPortal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = {OasisPortal.class})
@IntegrationTest
public class GeographicalAreaServiceTest {

    @Autowired
    private GeographicalAreaService service;

    @Autowired
    private MongoTemplate template;

    @Value("${application.geoarea.replication_test:false}")
    private boolean testReplication = false;

    @Test
    public void testReplicate() throws Exception {
        if(!testReplication) return; // skip replication testing since it is VERY long

        template.remove(new Query(), GeographicalArea.class);

        service.cache.replicate();

        assertNotEquals(template.find(new Query(), GeographicalArea.class).size(), 0);
    }
}