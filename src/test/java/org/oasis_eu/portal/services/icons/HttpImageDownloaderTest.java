package org.oasis_eu.portal.services.icons;

import org.junit.jupiter.api.Test;
import org.oasis_eu.portal.services.HttpImageDownloader;

import static org.junit.jupiter.api.Assertions.assertNull;

public class HttpImageDownloaderTest {

	// Note: it's harder to check that downloads do happen...

	HttpImageDownloader downloader = new HttpImageDownloader();

	@Test
	public void testNull() {
		assertNull(downloader.download(null));
		assertNull(downloader.download("http://www.google.com"));
	}

}