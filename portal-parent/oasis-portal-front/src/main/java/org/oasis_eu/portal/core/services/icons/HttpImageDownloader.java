package org.oasis_eu.portal.core.services.icons;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * User: schambon
 * Date: 9/2/14
 */
@Service
public class HttpImageDownloader implements ImageDownloader {

    // maximum image size that we're willing to download (1MB - which is already HUGE)
    private static final int MAX_RESOURCE_SIZE = 1048576;

    private static final Logger logger = LoggerFactory.getLogger(HttpImageDownloader.class);

    @Override
    public byte[] download(String iconUrl) {
        if (iconUrl == null) {
            logger.error("We are asked to download from a null URL");
            return null;
        }

        CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(1000)
                        .setSocketTimeout(1000).build())
                .build();
        try {
            HttpGet get = new HttpGet(iconUrl);
            try (CloseableHttpResponse response = client.execute(get)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    // Other 200-class statuses are discarded - we want authoritative responses that have associated
                    // content, only.
                    // At a pinch, we could allow 201 (Created), though the use-case for a dynamically-created icon
                    // (that is then cached by the portal) is unclear.
                    Header[] headers = response.getHeaders("Content-Length");
                    if (headers == null || headers.length == 0) {
                        logger.error("Icon URL {} specifies an unknown content-length; not downloading and providing default icon instead.", iconUrl);
                        return null;
                    }

                    int length = Integer.parseInt(headers[0].getValue());
                    if (length > MAX_RESOURCE_SIZE) {
                        logger.error("Icon URL {} points to a document with a very large content-length: {}; not downloading and providing default icon instead.", iconUrl, length);
                        return null;
                    }

                    // By then we know the file is at most 48 KB so we can download it to memory
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(length);

                    HttpEntity entity = response.getEntity();
                    entity.writeTo(bos);
                    byte[] iconBytes = bos.toByteArray();
                    return iconBytes;
                } else {
                    logger.error("Icon URL {} does not yield a 200 OK response ({} {})", iconUrl, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error("Cannot load icon from url: {}", iconUrl);
            return null;
        }
    }

}
