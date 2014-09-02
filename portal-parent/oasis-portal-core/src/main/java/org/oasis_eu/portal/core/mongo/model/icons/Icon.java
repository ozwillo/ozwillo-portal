package org.oasis_eu.portal.core.mongo.model.icons;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * User: schambon
 * Date: 8/21/14
 */
public class Icon {

    @Id
    private String id;

    // file name, for presentation reasons (eg citizenkin.png)
    private String filename;

    // original file URL
    @Indexed(unique = true)
    private String url;

    // format of the icon
    private IconFormat iconFormat = IconFormat.PNG_64BY64;

    // content of the icon
    private byte[] bytes;

    // SHA-256 hash of bytes, as hex
    private String hash;

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

    public IconFormat getIconFormat() {
        return iconFormat;
    }

    public void setIconFormat(IconFormat iconFormat) {
        this.iconFormat = iconFormat;
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
