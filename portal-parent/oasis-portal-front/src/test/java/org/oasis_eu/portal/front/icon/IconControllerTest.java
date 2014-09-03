package org.oasis_eu.portal.front.icon;

import com.google.common.io.ByteStreams;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.core.mongo.dao.icons.IconRepository;
import org.oasis_eu.portal.core.services.icons.IconDownloader;
import org.oasis_eu.portal.core.services.icons.IconService;
import org.oasis_eu.portal.main.OasisPortal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = {OasisPortal.class})
public class IconControllerTest {

    public static final String ICON_URL = "http://www.citizenkin.com/icon/one.png";
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private IconService iconService;

    @Value("${persistence.mongodatabase}")
    private String databaseName;

    private MockMvc mockMvc;
    private DBCollection collection;

    @Before
    public void setup() throws UnknownHostException {
        MongoClient mongo = new MongoClient("localhost");
        collection = mongo.getDB(databaseName).getCollection("icon");
        collection.drop();

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void testGetImage() throws Exception {
        // First the root controller will download the icon
        IconDownloader iconDownloader = mock(IconDownloader.class);
        byte[] bytes = load("images/64.png");
        when(iconDownloader.download(ICON_URL))
                .thenReturn(bytes);

        ReflectionTestUtils.setField(iconService, "iconDownloader", iconDownloader);

        String iconUri = iconService.getIconForURL(ICON_URL).toString();

        // verify that iconDownloader's download method has been called
        verify(iconDownloader).download(anyString());

        // strip out the protocol and host
        iconUri = iconUri.substring("http://localhost".length());

        Map<String, String> values = new HashMap<>();

        // download once
        mockMvc.perform(get(iconUri))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(bytes))
                .andDo(result -> values.put("etag", result.getResponse().getHeader("ETag")));

        // download a second time - check that the actual icon repository is not called
        IconRepository fakeRepo = mock(IconRepository.class);
        ReflectionTestUtils.setField(iconService, "iconRepository", fakeRepo);

        mockMvc.perform(get(iconUri).header("If-None-Match", values.get("etag")))
                .andExpect(status().is(304));

        verify(fakeRepo, never()).findOne(anyString());

        assertEquals(1, collection.count());
    }


    private byte[] load(String name) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        return ByteStreams.toByteArray(stream);
    }
}