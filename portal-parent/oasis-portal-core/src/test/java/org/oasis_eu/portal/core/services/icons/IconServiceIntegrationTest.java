package org.oasis_eu.portal.core.services.icons;

import com.google.common.io.ByteStreams;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.config.MongoConfiguration;
import org.oasis_eu.portal.core.mongo.dao.icons.IconDownloadAttemptRepository;
import org.oasis_eu.portal.core.mongo.dao.icons.IconRepository;
import org.oasis_eu.portal.core.mongo.model.icons.Icon;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IconServiceIntegrationTest.class, loader = AnnotationConfigContextLoader.class)
@Configuration
@PropertySource("classpath:test-application.properties")
@ComponentScan(basePackages = "org.oasis_eu.portal")
@Import(MongoConfiguration.class)
public class IconServiceIntegrationTest {

    private DBCollection blacklist;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Value("${persistence.mongodatabase}")
    private String databaseName;

    @Autowired
    private IconService iconService;

    private DB db;

    @Autowired
    private IconRepository iconRepository;

    @Autowired
    private IconDownloadAttemptRepository iconDownloadAttemptRepository;

    @Before
    public void clean() throws UnknownHostException {
        MongoClient mongo = new MongoClient("localhost");
        db = mongo.getDB(databaseName);
//        db.getCollection("icon").drop();
        blacklist = db.getCollection("icon_download_attempt");
//        blacklist.drop();

        iconRepository.deleteAll();
        iconDownloadAttemptRepository.deleteAll();

    }

    @Test
    @DirtiesContext
    public void testIconService() throws IOException {
        assertNotNull(iconService);

        IconDownloader downloader = mock(IconDownloader.class);
        when(downloader.download("http://www.citizenkin.com/icon/one.png")).thenReturn(load("images/64.png"));

        ReflectionTestUtils.setField(iconService, "iconDownloader", downloader);

        String iconUri = iconService.getIconForURL("http://www.citizenkin.com/icon/one.png");
        assertEquals(1, db.getCollection("icon").count());
        assertNotNull(iconUri);
        // test that this matches a regexp including a UUID
        Pattern pattern = Pattern.compile("http://localhost/icon/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/one.png");
        Matcher matcher = pattern.matcher(iconUri);
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
    public void testBlacklisting() throws Exception {
        IconDownloader downloader = mock(IconDownloader.class);
        when(downloader.download("http://www.citizenkin.com/icon/fake.png")).thenReturn(null);
        when(downloader.download("http://www.citizenkin.com/icon/rectangular.png")).thenReturn(load("images/rectangular.png"));
        when(downloader.download("http://www.citizenkin.com/icon/icon.tiff")).thenReturn(load("images/img-test.tiff"));

        ReflectionTestUtils.setField(iconService, "iconDownloader", downloader);

        assertEquals(0, blacklist.count());
        String uri = iconService.getIconForURL("http://www.citizenkin.com/icon/fake.png");
        assertEquals(defaultIcon(), uri);
        assertEquals(1, blacklist.count());

        iconService.getIconForURL("http://www.citizenkin.com/icon/rectangular.png");
        assertEquals(2, blacklist.count());

        iconService.getIconForURL("http://www.citizenkin.com/icon/icon.tiff");
        assertEquals(3, blacklist.count());

        iconService.getIconForURL("http://www.citizenkin.com/icon/fake.png");
        iconService.getIconForURL("http://www.citizenkin.com/icon/rectangular.png");
        iconService.getIconForURL("http://www.citizenkin.com/icon/icon.tiff");

        // check that we only called the downloader three times
        verify(downloader, times(3)).download(anyString());

        // the following really only tests MongoDB's TTL eviction code. It takes a couple minutes to run and doesn't
        // test our code, so let's forget about it for now.

//        LoggerFactory.getLogger(IconServiceIntegrationTest.class).info("Waiting for two minutes so MongoDB's TTL eviction kicks in - go brew a cup of tea or something.");
//
//        Thread.sleep(120000);
//
//        assertEquals(0, blacklist.count());
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

    private String defaultIcon() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = iconService.getClass().getDeclaredMethod("defaultIcon");
        method.setAccessible(true);
        return (String) method.invoke(iconService);
    }
}