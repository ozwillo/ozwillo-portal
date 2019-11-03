package org.oasis_eu.portal.services.icons;

import com.mongodb.client.MongoCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.oasis_eu.portal.config.environnements.helpers.KernelEnv;
import org.oasis_eu.portal.dao.ImageDownloadAttemptRepository;
import org.oasis_eu.portal.dao.ImageRepository;
import org.oasis_eu.portal.model.images.Image;
import org.oasis_eu.portal.model.images.ImageFormat;
import org.oasis_eu.portal.services.EnvPropertiesService;
import org.oasis_eu.portal.services.HttpImageDownloader;
import org.oasis_eu.portal.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ImageServiceIntegrationTest {

    private MongoCollection blacklist;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Autowired
    private ImageService imageService;

    @MockBean
    private HttpImageDownloader httpImageDownloader;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageDownloadAttemptRepository imageDownloadAttemptRepository;

    @MockBean
    private EnvPropertiesService envPropertiesService;

    @BeforeEach
    public void clean() {
        blacklist = mongoTemplate.getCollection("image_download_attempt");

        imageRepository.deleteAll();
        imageDownloadAttemptRepository.deleteAll();

        EnvConfig envConfig = new EnvConfig();
        envConfig.setBaseImageUrl("http://localhost:3000/media");
        envConfig.setBaseUrl("http://localhost:3000");
        KernelEnv kernelEnv = new KernelEnv();
        kernelEnv.setCallback_uri("http://localhost:3000/callback");
        envConfig.setKernel(kernelEnv);
        given(envPropertiesService.getCurrentConfig()).willReturn(envConfig);
    }

    @Test
    @DirtiesContext
    public void testIconService() throws IOException {

        when(httpImageDownloader.download("http://www.citizenkin.com/icon/one.png"))
                .thenReturn(load("images/64.png"));

        String iconUri = imageService.getImageForURL("http://www.citizenkin.com/icon/one.png", ImageFormat.PNG_64BY64, false);
        assertEquals(1, mongoTemplate.getCollection("image").count());
        assertNotNull(iconUri);
        // test that this matches a regexp including a UUID
        String applicationUrl = envPropertiesService.getCurrentConfig().getBaseUrl();
        Pattern pattern = Pattern.compile(applicationUrl + "/media/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/one.png");
        Matcher matcher = pattern.matcher(iconUri);
        assertTrue(matcher.matches());

        String id = matcher.group(1);
        assertNotNull(id);
        String hash = imageService.getHash(id);
        assertNotNull(hash);
        assertEquals("b357025fb8c2027cae8550b2e33df8f924d1aae35e2e5de4d4c14430636be6ab", hash);

        Optional<Image> image = imageService.getImage(id);
        assertTrue(image.isPresent());
        assertEquals(hash, image.get().getHash());
    }

    @Test
    public void testBlacklisting() throws Exception {

        when(httpImageDownloader.download("http://www.citizenkin.com/icon/fake.png")).thenReturn(null);
        when(httpImageDownloader.download("http://www.citizenkin.com/icon/rectangular.png")).thenReturn(load("images/rectangular.png"));
        when(httpImageDownloader.download("http://www.citizenkin.com/icon/icon.tiff")).thenReturn(load("images/img-test.tiff"));

        assertEquals(0, blacklist.countDocuments());

        imageService.getImageForURL("http://www.citizenkin.com/icon/rectangular.png", ImageFormat.PNG_64BY64, false);
        assertEquals(1, blacklist.countDocuments());

        imageService.getImageForURL("http://www.citizenkin.com/icon/icon.tiff", ImageFormat.PNG_64BY64, false);
        assertEquals(2, blacklist.countDocuments());

        imageService.getImageForURL("http://www.citizenkin.com/icon/fake.png", ImageFormat.PNG_64BY64, false);
        imageService.getImageForURL("http://www.citizenkin.com/icon/rectangular.png", ImageFormat.PNG_64BY64, false);
        imageService.getImageForURL("http://www.citizenkin.com/icon/icon.tiff", ImageFormat.PNG_64BY64, false);

        // check that we only called the downloader three times
        verify(httpImageDownloader, times(3)).download(anyString());
    }

    @Test
    public void testDummyData() {
        assertNull(imageService.getHash(UUID.randomUUID().toString()));
        assertFalse(imageService.getImage(UUID.randomUUID().toString()).isPresent());
    }

    private byte[] load(String name) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        return StreamUtils.copyToByteArray(stream);
    }
}
