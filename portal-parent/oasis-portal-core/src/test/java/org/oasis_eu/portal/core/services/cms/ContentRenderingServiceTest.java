package org.oasis_eu.portal.core.services.cms;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.config.MongoConfiguration;
import org.oasis_eu.portal.core.config.PortalCoreConfiguration;
import org.oasis_eu.portal.core.constants.OasisLocales;
import org.oasis_eu.portal.core.mongo.dao.cms.ContentItemRepository;
import org.oasis_eu.portal.core.mongo.model.cms.ContentItem;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;


import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ContentRenderingServiceTest.class, loader = AnnotationConfigContextLoader.class)
@Configuration
@PropertySource("classpath:test-application.properties")
@Import({MongoConfiguration.class, PortalCoreConfiguration.class})
public class ContentRenderingServiceTest {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Autowired
    private ContentItemRepository repository;

    @Autowired
    private ContentRenderingService contentRenderingService;

    @Before
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    public void testRender() {
        ContentItem item = new ContentItem();
        item.setId("home");
        item.getContent().put("en", "Hello world\n===========\n\nThis is a test. Enjoy.");
        item.getContent().put("fr", "Bonjour ici\n===========\n\nCeci est un test. Il est beau mon test.");

        repository.save(item);

        String render = contentRenderingService.render("home", OasisLocales.FRENCH);
        LoggerFactory.getLogger(ContentRenderingServiceTest.class).info(render);
        assertTrue(render.contains("Bonjour"));
    }
}