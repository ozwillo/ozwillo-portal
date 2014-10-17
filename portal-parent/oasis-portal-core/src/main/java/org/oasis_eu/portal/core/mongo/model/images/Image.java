package org.oasis_eu.portal.core.mongo.model.images;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * User: schambon
 * Date: 8/21/14
 */
@Document(collection = "image")
public class Image {

    @Id
    private String id;

    // file name, for presentation reasons (eg citizenkin.png)
    private String filename;

    // original file URL
    @Indexed(unique = true)
    private String url;

    // format of the icon
    private ImageFormat imageFormat = ImageFormat.PNG_64BY64;

    // content of the icon
    private byte[] bytes;

    // SHA-256 hash of bytes, as hex
    private String hash;

    private DateTime downloadedTime = new DateTime(0);

    public DateTime getDownloadedTime() {
        return downloadedTime;
    }

    public void setDownloadedTime(DateTime downloadedTime) {
        this.downloadedTime = downloadedTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public ImageFormat getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(ImageFormat imageFormat) {
        this.imageFormat = imageFormat;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
