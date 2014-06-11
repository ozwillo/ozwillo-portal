package org.oasis_eu.portal.core.services.mongo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.config.MongoConfiguration;
import org.oasis_eu.portal.core.mongo.dao.cms.ContentItemRepository;
import org.oasis_eu.portal.core.mongo.model.cms.ContentItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * User: schambon
 * Date: 6/11/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ContentItemRepositoryTest.class, loader = AnnotationConfigContextLoader.class)
@Configuration
@PropertySource("classpath:test-application.properties")
@Import(MongoConfiguration.class)
public class ContentItemRepositoryTest {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Value("${persistence.mongodatabase}")
    private String databaseName;

    @Autowired
    private ContentItemRepository repository;

    @Before
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    public void testSpringConf() {
        assertEquals("portal_test", databaseName);
    }

    @Test
    public void testCRUD() {

        ContentItem item = new ContentItem();
        item.setId("home");
        item.getContent().put("en", "Hello world\n===========\n\nThis is a test. Enjoy.");
        item.getContent().put("fr", "Bonjour ici\n===========\n\nCeci est un test. Il est beau mon test.");

        repository.save(item);

        ContentItem found = repository.findOne("home");
        assertNotNull(found);
        assertEquals("Hello world\n" +
                "===========\n" +
                "\n" +
                "This is a test. Enjoy.", found.getContent().get("en"));


    }


}
