package org.oasis_eu.portal.controller.icon;

import com.mongodb.client.MongoCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_eu.portal.dao.ImageRepository;
import org.oasis_eu.portal.model.images.ImageFormat;
import org.oasis_eu.portal.services.HttpImageDownloader;
import org.oasis_eu.portal.services.ImageService;
import org.oasis_eu.portal.OzwilloPortal;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = {OzwilloPortal.class})
public class ImageControllerTest {

	private static final String ICON_URL = "http://www.citizenkin.com/icon/one.png";

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private ImageService imageService;

	@Value("${application.url}")
	private String applicationUrl;

	@Autowired
	private MongoTemplate mongoTemplate;

	private MockMvc mockMvc;
	private MongoCollection collection;

	@Before
	public void setup() {
		collection = mongoTemplate.getCollection("image");
		collection.drop();

		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		OpenIdCAuthentication authentication = new OpenIdCAuthentication("test", "accesstoken", "idtoken", java.time.Instant.now(), java.time.Instant.now().plus(24, ChronoUnit.HOURS), true, false);
		SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
		SecurityContextHolder.getContext().setAuthentication(authentication);

	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testGetImage() throws Exception {
		// First the root controller will download the icon
		HttpImageDownloader imageDownloader = mock(HttpImageDownloader.class);
		byte[] bytes = load("images/64.png");
		when(imageDownloader.download(ICON_URL)).thenReturn(bytes);

		ReflectionTestUtils.setField(imageService, "imageDownloader", imageDownloader);

		String iconUri = imageService.getImageForURL(ICON_URL, ImageFormat.PNG_64BY64, false);

		// verify that iconDownloader's download method has been called
		verify(imageDownloader).download(anyString());

		// strip out the protocol and host
		iconUri = iconUri.substring(applicationUrl.length());

		Map<String, String> values = new HashMap<>();

		// download once
		mockMvc.perform(get(iconUri))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().contentType(MediaType.IMAGE_PNG))
				.andExpect(content().bytes(bytes))
				.andExpect(header().string("Cache-Control", "public, max-age=31536000"))
				.andDo(result -> values.put("etag", result.getResponse().getHeader("ETag")));

		// download a second time - check that the actual icon repository is not called
		ImageRepository fakeRepo = mock(ImageRepository.class);
		ReflectionTestUtils.setField(imageService, "imageRepository", fakeRepo);

		mockMvc.perform(get(iconUri).header("If-None-Match", values.get("etag")))
				.andExpect(status().is(304));

		verify(fakeRepo, never()).findById(anyString());

		assertEquals(1, collection.count());
	}


	private byte[] load(String name) throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
		return StreamUtils.copyToByteArray(stream);
	}
}
