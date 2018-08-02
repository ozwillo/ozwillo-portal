package org.oasis_eu.portal.services.icons;

import org.junit.Test;
import org.oasis_eu.portal.model.images.ImageFormat;
import org.oasis_eu.portal.services.ImageService;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.*;

public class ImageServiceTest {

	@Test
	public void testGetFileName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		assertEquals("www.whatever.com", doGetFileName("http://www.whatever.com"));
		assertEquals("citizenkin.png", doGetFileName("http://www.citizenkin.com/icon/citizenkin.png"));
		assertEquals("www.plouf.org", doGetFileName("http://www.plouf.org/"));
		assertEquals("citizenkin.png", doGetFileName("http://www.citizenkin.com/icon/citizenkin.png?size=bis"));
		assertEquals("citizenkin.png", doGetFileName("https://www.citizenkin.com/icon/citizenkin.png"));
		assertEquals("file.txt", doGetFileName("file.txt"));
		assertEquals("file.txt", doGetFileName("file.txt?param=true"));
		assertEquals("file.txt", doGetFileName("file.txt/"));
	}

	@Test
	public void testEnsurePNG() throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		assertTrue(doEnsurePNG(load("images/img-16.png")));
		assertFalse(doEnsurePNG(load("images/img-01.svg")));
		assertFalse(doEnsurePNG(load("images/img-test.tiff")));
		assertFalse(doEnsurePNG(new byte[0]));
	}

	@Test
	public void testGetFormat() throws Exception {
		assertEquals(ImageFormat.PNG_64BY64, doGetFormat(load("images/64.png")));
		assertEquals(ImageFormat.PNG_32BY32, doGetFormat(load("images/32.png")));
		assertEquals(ImageFormat.INVALID, doGetFormat(load("images/img-16.png")));

		byte[] b = new byte[1024];
		Random random = new Random();
		random.nextBytes(b);
		assertEquals(ImageFormat.INVALID, doGetFormat(b));

		assertEquals(ImageFormat.INVALID, doGetFormat(load("images/rectangular.png")));
	}

	@Test
	public void testGetHash() throws Exception {
		String hash = doGetHash(load("images/64.png"));
		assertNotNull(hash);
		assertEquals(64, hash.length());

		// 64-bis is only different from 64 by one pixel
		String hash2 = doGetHash(load("images/64-bis.png"));
		assertNotEquals(hash, hash2);
		// 64-ter is really the same as 64
		assertEquals(hash, doGetHash(load("images/64-ter.png")));
	}


	private String doGetFileName(String arg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		ImageService service = new ImageService();
		Method getFileName = service.getClass().getDeclaredMethod("getFileName", String.class);
		getFileName.setAccessible(true);

		return (String) getFileName.invoke(service, arg);
	}

	private boolean doEnsurePNG(byte[] array) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		ImageService service = new ImageService();
		Method ensurePNG = service.getClass().getDeclaredMethod("ensurePNG", byte[].class);
		ensurePNG.setAccessible(true);

		return (boolean) ensurePNG.invoke(service, array);
	}

	private ImageFormat doGetFormat(byte[] array) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		ImageService service = new ImageService();
		Method getFormat = service.getClass().getDeclaredMethod("getFormat", byte[].class);
		getFormat.setAccessible(true);
		return (ImageFormat) getFormat.invoke(service, array);
	}

	private String doGetHash(byte[] array) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		ImageService service = new ImageService();
		Method getHash = service.getClass().getDeclaredMethod("getHash", byte[].class);
		getHash.setAccessible(true);
		return (String) getHash.invoke(service, array);
	}


	private byte[] load(String name) throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
		return StreamUtils.copyToByteArray(stream);
	}
}