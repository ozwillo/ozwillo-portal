package org.oasis_eu.portal.core.services.icons;

import com.google.common.base.Strings;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tika.Tika;
import org.oasis_eu.portal.core.mongo.dao.icons.IconRepository;
import org.oasis_eu.portal.core.mongo.model.icons.Icon;
import org.oasis_eu.portal.core.mongo.model.icons.IconFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * User: schambon
 * Date: 8/21/14
 */

@Service
public class IconService {
    private static final Logger logger = LoggerFactory.getLogger(IconService.class);

    @Autowired
    private IconDownloader iconDownloader;

    @Autowired
    private IconRepository iconRepository;

    @Value("${application.baseIconUrl}")
    private String baseIconUrl;

    @Value("${application.defaultIconUrl}")
    private String defaultIconUrl;

    public URI getIconForURL(String iconUrl) {

        Icon icon = iconRepository.findByUrl(iconUrl);

        if (icon == null) {

            // 1. download the icon
            byte[] iconBytes = iconDownloader.download(iconUrl);
            if (iconBytes == null) {
                logger.error("Could not load icon from URL {}, returning default", iconUrl);
                return defaultIcon();
            }

            // 2. make sure it is a 64x64 PNG (NB this will change in the future with more intelligent format detection / conversion)
            if (!ensurePNG(iconBytes)) {
                logger.error("Icon URL {} is not a PNG, returning default icon", iconUrl);
                return defaultIcon();
            }
            if (!IconFormat.PNG_64BY64.equals(getFormat(iconBytes))) {
                logger.error("Icon URL {} does not point to a 64×64 image, returning default icon", iconUrl);
                return defaultIcon();
            }

            // 3. compute the hash and store the icon
            icon = new Icon();
            icon.setId(UUID.randomUUID().toString());
            icon.setBytes(iconBytes);
            icon.setUrl(iconUrl);
            icon.setIconFormat(IconFormat.PNG_64BY64);
            icon.setFilename(getFileName(iconUrl));
            icon.setHash(getHash(iconBytes));

            icon = iconRepository.save(icon);
        }

        return UriComponentsBuilder.fromHttpUrl(baseIconUrl)
                .path("/")
                .path(icon.getId())
                .path("/")
                .path(icon.getFilename())
                .build()
                .toUri();
    }

    private URI defaultIcon() {
        try {
            return new URI(defaultIconUrl);
        } catch (URISyntaxException e1) {
            logger.error("Cannot load DEFAULT icon, this is FATAL", e1);
            return null;
        }
    }

    private String getFileName(String url) {

        if (Strings.isNullOrEmpty(url)) {
            return "";
        }

        if (url.contains("?")) {
            url = url.substring(0, url.indexOf('?'));
        }

        while (url.endsWith("/")) {
            url = stripSlash(url);
        }

        return url.substring(url.lastIndexOf("/") + 1);
    }

    private String stripSlash(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return "";
        } else if (input.endsWith("/")) {
            return input.substring(0, input.length() - 1);
        } else return input;
    }


    private boolean ensurePNG(byte[] array) {
        Tika tika = new Tika();
        try {
            String found = tika.detect(new ByteArrayInputStream(array));
            return "image/png".equals(found);
        } catch (IOException e) {
            logger.error("Image file cannot be analyzed", e);
        }
        return false;
    }

    private IconFormat getFormat(byte[] image) {
        try {
            BufferedImage bim = ImageIO.read(new ByteArrayInputStream(image));
            if (bim == null) {
                logger.error("Cannot read image");
                return IconFormat.INVALID;
            }
            int height = bim.getHeight();
            int width = bim.getWidth();

            if (height != width) {
                logger.error("Can only handle square icons, got {}×{}",  width, height);
                return IconFormat.INVALID;
            }

            switch (height) {
                case 16:
                    return IconFormat.PNG_16BY16;
                case 32:
                    return IconFormat.PNG_32BY32;
                case 64:
                    return IconFormat.PNG_64BY64;
                case 128:
                    return IconFormat.PNG_128BY128;
                case 256:
                    return IconFormat.PNG_256BY256;
                default:
                    logger.error("Image has size {}×{} - which is invalid", height, width);
                    return IconFormat.INVALID;
            }

        } catch (IOException e) {
            logger.error("Cannot read image", e);
            return IconFormat.INVALID;
        }
    }

    private String getHash(byte[] array) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return toHex(messageDigest.digest(array));
        } catch (NoSuchAlgorithmException e) {

            logger.error("Cannot load SHA-256 in this JVM!?", e);
            return toHex(new byte[32]);
        }

    }

    private String toHex(byte[] array) {
        StringBuilder s = new StringBuilder();
        for (byte b: array) {
            s.append(String.format("%02x", b));
        }
        return s.toString();
    }
}
