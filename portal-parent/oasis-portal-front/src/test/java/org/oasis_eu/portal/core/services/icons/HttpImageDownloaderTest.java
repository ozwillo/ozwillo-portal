package org.oasis_eu.portal.core.services.icons;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class HttpImageDownloaderTest {

    // Note: it's harder to check that downloads do happen...

    ImageDownloader downloader = new HttpImageDownloader();

    @Test
    public void testNull() {
        assertNull(downloader.download(null));
        assertNull(downloader.download("http://www.google.com"));
    }

}