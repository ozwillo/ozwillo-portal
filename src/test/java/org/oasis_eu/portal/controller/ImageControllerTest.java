package org.oasis_eu.portal.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.oasis_eu.portal.config.environnements.helpers.KernelEnv;
import org.oasis_eu.portal.model.images.Image;
import org.oasis_eu.portal.services.EnvPropertiesService;
import org.oasis_eu.portal.services.ImageService;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
public class ImageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ImageService imageService;

	@MockBean
	private EnvPropertiesService envPropertiesService;

	@BeforeEach
	public void setup() {

		EnvConfig envConfig = new EnvConfig();
		envConfig.setBaseImageUrl("http://localhost:3000/media");
		envConfig.setBaseUrl("http://localhost:3000");
		KernelEnv kernelEnv = new KernelEnv();
		kernelEnv.setCallback_uri("http://localhost:3000/callback");
		envConfig.setKernel(kernelEnv);
		given(envPropertiesService.getCurrentConfig()).willReturn(envConfig);
	}

	@Test
	public void getAndCacheImage() throws Exception {
		byte[] bytes = load("images/64.png");
		String iconHash = "\"icon-hash\"";

		Image image = new Image();
		image.setBytes(bytes);
		image.setHash(iconHash);

		given(imageService.getImage("icon-id"))
				.willReturn(Optional.of(image));

		// no If-None-Match header provided, will return the image
		this.mockMvc.perform(get("/media/icon-id/icon-name").servletPath("/media/icon-id/icon-name"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().contentType(MediaType.IMAGE_PNG))
				.andExpect(header().string("Cache-Control", "max-age=31536000, public"))
				.andExpect(content().bytes(bytes))
				.andExpect(header().string("ETag", "\"" + iconHash + "\""));

		given(imageService.getHash("icon-id"))
				.willReturn(iconHash);

		// download a second time - check that the actual icon repository is not called
		this.mockMvc.perform(get("/media/icon-id/icon-name").servletPath("/media/icon-id/icon-name").header("If-None-Match", iconHash))
				.andExpect(status().isNotModified())
				.andExpect(header().exists("Cache-Control"));

		verify(imageService).getImage(anyString());
		verify(imageService).getHash(anyString());
	}

	@Test
	public void notFoundImage() throws Exception {
		given(imageService.getImage("icon-id"))
				.willThrow(ImageController.IconNotFound.class);

		this.mockMvc.perform(get("/media/unknown-icon-id/icon-name").servletPath("/media/unknown-icon-id/icon-name"))
				.andExpect(status().isNotFound());

		verify(imageService).getImage(anyString());
	}

	@Test
	public void createWithNoFile() throws Exception {

		this.mockMvc.perform(multipart("/media/objectIcon/icon-id").file("iconFile", null)
				.servletPath("/media/objectIcon/icon-id").with(csrf()))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createWithNoMultipart() throws Exception {

		this.mockMvc.perform(post("/media/objectIcon/icon-id")
				.servletPath("/media/objectIcon/icon-id").with(csrf()))
				.andExpect(status().isInternalServerError());
	}

	@Test
	public void createImage() throws Exception {

		given(imageService.storeImageForObjectId("icon-id", new Image()))
				.willReturn(new Image());
		given(imageService.buildImageServedUrl(any()))
				.willReturn("http://localhost:3000/media/icon-id/64.png");

		this.mockMvc.perform(multipart("/media/objectIcon/icon-id")
				.file("iconFile", load("images/64.png"))
				.servletPath("/media/objectIcon/icon-id").with(csrf()))
				.andExpect(status().isOk())
				.andExpect(header().string("Location", "http://localhost:3000/media/icon-id/64.png"));

		verify(imageService).storeImageForObjectId(anyString(), any());
		verify(imageService).buildImageServedUrl(any());
	}

	@Test
	public void createWrongImage() throws Exception {

		given(imageService.storeImageForObjectId(eq("icon-id"), any()))
				.willThrow(WrongQueryException.class);

		this.mockMvc.perform(multipart("/media/objectIcon/icon-id")
				.file("iconFile", load("images/64.png"))
				.servletPath("/media/objectIcon/icon-id").with(csrf()))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("error.msg.action-cant-be-done"));

		verify(imageService).storeImageForObjectId(anyString(), any());
	}

	private byte[] load(String name) throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
		return StreamUtils.copyToByteArray(stream);
	}
}
