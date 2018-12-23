package org.oasis_eu.portal.controller.icon;

import com.mongodb.client.MongoCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.oasis_eu.portal.config.environnements.helpers.KernelEnv;
import org.oasis_eu.portal.dao.ImageRepository;
import org.oasis_eu.portal.model.images.ImageFormat;
import org.oasis_eu.portal.services.EnvPropertiesService;
import org.oasis_eu.portal.services.HttpImageDownloader;
import org.oasis_eu.portal.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ImageControllerTest {

	private static final String ICON_URL = "http://www.citizenkin.com/icon/one.png";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ImageService imageService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@MockBean
	private EnvPropertiesService envPropertiesService;

	private MongoCollection collection;

	@BeforeEach
	public void setup() {
		collection = mongoTemplate.getCollection("image");
		collection.drop();

		EnvConfig envConfig = new EnvConfig();
		envConfig.setBaseImageUrl("http://localhost:3000/media");
		envConfig.setBaseUrl("http://localhost:3000");
		KernelEnv kernelEnv = new KernelEnv();
		kernelEnv.setCallback_uri("http://localhost:3000/callback");
		envConfig.setKernel(kernelEnv);
		given(envPropertiesService.getCurrentConfig()).willReturn(envConfig);
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
		String applicationUrl = envPropertiesService.getCurrentConfig().getBaseUrl();
		iconUri = iconUri.substring(applicationUrl.length());

		Map<String, String> values = new HashMap<>();

		// download once
		this.mockMvc.perform(get(iconUri).servletPath(iconUri))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().contentType(MediaType.IMAGE_PNG))
				.andExpect(header().string("Cache-Control", "public, max-age=31536000"))
				.andExpect(content().bytes(bytes))
				.andDo(result -> values.put("etag", result.getResponse().getHeader("ETag")));

		// download a second time - check that the actual icon repository is not called
		ImageRepository fakeRepo = mock(ImageRepository.class);
		ReflectionTestUtils.setField(imageService, "imageRepository", fakeRepo);

		this.mockMvc.perform(get(iconUri).servletPath(iconUri).header("If-None-Match", values.get("etag")))
				.andExpect(status().isNotModified());

		verify(fakeRepo, never()).findById(anyString());

		assertEquals(1, collection.count());
	}

	private byte[] load(String name) throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
		return StreamUtils.copyToByteArray(stream);
	}
}
