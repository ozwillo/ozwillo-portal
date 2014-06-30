package org.oasis_eu.portal.core.mongo.model.cms;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * User: schambon
 * Date: 6/11/14
 */
@Document(collection = "content_item")
public class ContentItem implements Comparable<ContentItem> {

    @Id
    private String id;

    private Map<String, String> content = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    public int compareTo(ContentItem o) {
        return id.compareTo(o.getId());
    }
    
}
