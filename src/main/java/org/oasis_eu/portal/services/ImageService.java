package org.oasis_eu.portal.services;

import org.apache.tika.Tika;
import org.joda.time.DateTime;
import org.oasis_eu.portal.config.environnements.helpers.EnvConfig;
import org.oasis_eu.portal.dao.DirectAccessImageRepo;
import org.oasis_eu.portal.dao.ImageDownloadAttemptRepository;
import org.oasis_eu.portal.dao.ImageRepository;
import org.oasis_eu.portal.model.images.Image;
import org.oasis_eu.portal.model.images.ImageDownloadAttempt;
import org.oasis_eu.portal.model.images.ImageFormat;
import org.oasis_eu.spring.kernel.exception.WrongQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

/**
 * User: schambon
 * Date: 8/21/14
 */

@Service
public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    /**
     * used to build "virtual", not served unique icon URL
     */
    public static final String ICONIMAGE_NAME = "icon.png";
    /**
     * used to build "virtual", not served unique icon URL
     */
    public static final String OBJECTICONIMAGE_PATHELEMENT = "objectIcon";

    @Autowired
    private HttpImageDownloader imageDownloader;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private DirectAccessImageRepo directAccessImageRepo;

    @Autowired
    private ImageDownloadAttemptRepository imageDownloadAttemptRepository;

    @Autowired
    private EnvPropertiesService envPropertiesService;


    /**
     * Allows to store a single image / icon per object to prettify its display
     *
     * @param objectId     id of ex. user service subscription, user...
     * @param imageToStore imageToStore with only bytes (filename will be "icon.png"
     * @return complete saved image (with also id, hash, servedImageUrl also used as inputUrl...)
     */
    public Image storeImageForObjectId(String objectId, Image imageToStore) {
        byte[] iconBytes = imageToStore.getBytes();
        if (iconBytes == null || imageToStore.getFilename() == null) {
            throw new WrongQueryException("Image must have bytes and name", HttpStatus.BAD_REQUEST.value());
        }

        // 1. build servedImageUrl, also used as "proxied" / cached inputUrl
        String servedImageUrl = buildObjectIconImageVirtualUrl(objectId);

        // 2. make sure it is a 64x64 PNG (NB this will change in the future with more intelligent format detection / conversion)
        ImageFormat format = getFormat(iconBytes);
        if (!ensurePNG(iconBytes)) {
            logger.error("Icon URL {} is not a PNG, returning default icon", servedImageUrl);
            throw new WrongQueryException("Image is not a PNG", HttpStatus.BAD_REQUEST.value());
        }

        if (format == ImageFormat.INVALID) {
            logger.error("Invalid format of image for object.");
            throw new WrongQueryException("Image format is invalid", HttpStatus.BAD_REQUEST.value());
        }

        Image image = imageRepository.findByUrl(servedImageUrl);
        if (image == null) {
            image = imageToStore; // new one
            image.setId(UUID.randomUUID().toString());
        }

        // 3. compute the hash and store the icon
        image.setUrl(servedImageUrl); // servedImageUrl used as inputUrl, required in this collection, guarantees only one image per object
        image.setImageFormat(format);
        image.setBytes(iconBytes);
        image.setHash(getHash(iconBytes));
        image.setDownloadedTime(DateTime.now());
        image.setFilename(URLEncoder.encode(imageToStore.getFilename(), StandardCharsets.UTF_8));

        return imageRepository.save(image);
    }

    /**
     * Computes URL where image (business object icon) is served in the case where the original
     * (be it proxied or uploaded) URL is unavailable because NOT stored in the business object.
     * To be used only when storing it, otherwise to get image use buildObjectIconImageVirtualUrlOrNullIfNone().
     *
     * @param objectId of object whose icon we're trying to build the virtual URL
     * @return URL that is unique for mongo, but not where this image will be served
     */
    private String buildObjectIconImageVirtualUrl(String objectId) {
        String baseImageUrl = envPropertiesService.getCurrentConfig().getBaseImageUrl();
        return UriComponentsBuilder.fromHttpUrl(baseImageUrl)
            .path("/")
            .path(OBJECTICONIMAGE_PATHELEMENT)
            .path("/")
            .path(objectId)
            .path("/")
            .path(ICONIMAGE_NAME) // .path(image.getFilename())
            .build()
            .toUriString();
    }

    /**
     * @param image (only id & filename params are required)
     * @return URL where this image will be served
     */
    public String buildImageServedUrl(Image image) {
        String baseImageUrl = envPropertiesService.getCurrentConfig().getBaseImageUrl();
        return UriComponentsBuilder.fromHttpUrl(baseImageUrl)
            .path("/")
            .path(image.getId())
            .path("/")
            .path(image.getFilename())
            .build()
            .toUriString();
    }

    public String getImageForURL(String inputUrl, ImageFormat format, boolean force) {
        return getImageForURLInEnvConfig(inputUrl, format, force, envPropertiesService.getCurrentConfig());
    }

    /**
     * Retrieves URL where image is served in the case where the original (be it proxied
     * or uploaded) URL is stored in the business object
     *
     * @param inputUrl the original (be it proxied or uploaded) URL of the image
     * @param force whether to force downloading image even it already present in repository
     */
    public String getImageForURLInEnvConfig(String inputUrl, ImageFormat format, boolean force, EnvConfig envConfig) {
        String baseImageUrl = envConfig.getBaseImageUrl();
        String defaultIconUrl = envConfig.getDefaultIconUrl();

        if (inputUrl != null && inputUrl.startsWith(baseImageUrl)) {
            // Self-served (Portal object icon) image
            return inputUrl;
        }

        if (imageDownloadAttemptRepository.findByUrl(inputUrl) != null) {
            logger.debug("Image input URL {} is blacklisted, returning default icon", inputUrl);
            return defaultIconUrl;
        }

        Image image = imageRepository.findByUrl(inputUrl);

        if (image == null || force) {

            // 1. download the icon
            byte[] iconBytes = imageDownloader.download(inputUrl);
            if (iconBytes == null) {
                logger.error("Could not load icon from URL {}, returning default", inputUrl);
                blacklist(inputUrl);
                return defaultIconUrl;
            }

            // 2. make sure it is a 64x64 PNG (NB this will change in the future with more intelligent format detection / conversion)
            if (!ensurePNG(iconBytes)) {
                logger.error("Icon URL {} is not a PNG, returning default icon", inputUrl);
                blacklist(inputUrl);
                return defaultIconUrl;
            }
            if (!format.equals(getFormat(iconBytes))) {
                logger.error("Icon URL {} does not point to an image of correct format ({}), returning default icon", inputUrl, format);
                blacklist(inputUrl);
                return defaultIconUrl;
            }

            // 3. compute the hash and store the icon
            if (image == null) {
                image = new Image();
                image.setId(UUID.randomUUID().toString());
                image.setUrl(inputUrl);
                image.setImageFormat(format);
                image.setFilename(getFileName(inputUrl));
            }
            image.setBytes(iconBytes);
            image.setHash(getHash(iconBytes));
            image.setDownloadedTime(DateTime.now());

            image = imageRepository.save(image);
        }

        return buildImageServedUrl(image);
    }

    public Optional<Image> getImage(String id) {
        return imageRepository.findById(id);
    }

    public String getHash(String id) {
        return directAccessImageRepo.getHashForIcon(id);
    }

    public boolean isDefaultIcon(String icon) {
        String defaultIconUrl = envPropertiesService.getCurrentConfig().getDefaultIconUrl();
        return defaultIconUrl.equals(icon);
    }

    private String getFileName(String url) {

        if (!StringUtils.hasLength(url)) {
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
        if (!StringUtils.hasLength(input)) {
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

    private ImageFormat getFormat(byte[] image) {
        try {
            BufferedImage bim = ImageIO.read(new ByteArrayInputStream(image));
            if (bim == null) {
                logger.error("Cannot read image");
                return ImageFormat.INVALID;
            }
            int height = bim.getHeight();
            int width = bim.getWidth();

            String formatString = String.format("%sx%s", width, height);

            switch (formatString) {
                case "16x16":
                    return ImageFormat.PNG_16BY16;
                case "32x32":
                    return ImageFormat.PNG_32BY32;
                case "64x64":
                    return ImageFormat.PNG_64BY64;
                case "128x128":
                    return ImageFormat.PNG_128BY128;
                case "256x256":
                    return ImageFormat.PNG_256BY256;
                case "800x450":
                    return ImageFormat.PNG_800BY450;
                default:
                    logger.error("Cannot handle image format: {}", formatString);
                    return ImageFormat.INVALID;
            }

        } catch (IOException e) {
            logger.error("Cannot read image", e);
            return ImageFormat.INVALID;
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
        for (byte b : array) {
            s.append(String.format("%02x", b));
        }
        return s.toString();
    }

    private void blacklist(String url) {
        ImageDownloadAttempt attempt = new ImageDownloadAttempt(url);

        if (imageDownloadAttemptRepository.findByUrl(url) == null) { // checking is overkill, but in case of multiple threads doing the same thing, let's keep it easy
            logger.info("Blacklisting URL {}", url);
            imageDownloadAttemptRepository.save(attempt);
        } else {
            logger.debug("URL {} is already blacklisted", url);
        }
    }
}
