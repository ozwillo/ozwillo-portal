package org.oasis_eu.portal.core.mongo.model.images;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * User: schambon
 * Date: 9/3/14
 */
@Document(collection = "image_download_attempt")
public class ImageDownloadAttempt {

    @Id
    private String id;

    @Indexed(unique = true)
    private String url;

    @Indexed(expireAfterSeconds = 900)
    private DateTime time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
