package org.oasis_eu.portal.core.services.icons;

import com.google.common.io.ByteStreams;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.config.MongoConfiguration;
import org.oasis_eu.portal.core.mongo.model.icons.Icon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IconServiceIntegrationTest.class, loader = AnnotationConfigContextLoader.class)
@Configuration
@PropertySource("classpath:test-application.properties")
@ComponentScan(basePackages = "org.oasis_eu.portal")
@Import(MongoConfiguration.class)
public class IconServiceIntegrationTest {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Value("${persistence.mongodatabase}")
    private String databaseName;

    @Autowired
    private IconService iconService;

    private DB db;

    @Before
    public void clean() throws UnknownHostException {
        MongoClient mongo = new MongoClient("localhost");
        db = mongo.getDB(databaseName);
        db.getCollection("icon").drop();
    }

    @Test
    public void testIconService() throws IOException {
        assertNotNull(iconService);

        IconDownloader downloader = mock(IconDownloader.class);
        when(downloader.download("http://www.citizenkin.com/icon/one.png")).thenReturn(load("images/64.png"));

        ReflectionTestUtils.setField(iconService, "iconDownloader", downloader);

        URI iconUri = iconService.getIconForURL("http://www.citizenkin.com/icon/one.png");
        assertEquals(1, db.getCollection("icon").count());
        assertNotNull(iconUri);
        String uriString = iconUri.toString();
        // test that this matches a regexp including a UUID
        Pattern pattern = Pattern.compile("http://localhost/icon/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/one.png");
        Matcher matcher = pattern.matcher(uriString);
        assertTrue(matcher.matches());

        String id = matcher.group(1);
        assertNotNull(id);
        String hash = iconService.getHash(id);
        assertNotNull(hash);
        assertEquals("b357025fb8c2027cae8550b2e33df8f924d1aae35e2e5de4d4c14430636be6ab", hash);

        Icon icon = iconService.getIcon(id);
        assertEquals(hash, icon.getHash());
    }

    @Test
    public void testDummyData() throws Exception {
        assertNull(iconService.getHash(UUID.randomUUID().toString()));
        assertNull(iconService.getIcon(UUID.randomUUID().toString()));
    }

    private byte[] load(String name) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        return ByteStreams.toByteArray(stream);
    }
}