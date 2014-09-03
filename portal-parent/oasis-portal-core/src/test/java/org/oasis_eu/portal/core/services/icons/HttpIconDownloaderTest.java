package org.oasis_eu.portal.core.services.icons;

import org.junit.Test;

import static org.junit.Assert.*;

public class HttpIconDownloaderTest {

    // Note: it's harder to check that downloads do happen...

    IconDownloader downloader = new HttpIconDownloader();

    @Test
    public void testNull() {
        assertNull(downloader.download(null));
        assertNull(downloader.download("http://www.google.com"));
    }

}