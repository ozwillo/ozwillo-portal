package org.oasis_eu.portal.model.store;

import org.oasis_eu.portal.model.kernel.store.CatalogEntryType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * User: schambon
 * Date: 10/31/14
 */
@Document(collection = "rating")
@CompoundIndexes({
    @CompoundIndex(name = "appType_appId", def = "{'appType':1, 'appId':1}"),
    @CompoundIndex(name = "appType_appId_userId", def = "{'appType':1, 'appId':1, 'userId':1}")
})
public class Rating {

    @Id
    private String id;

    private CatalogEntryType appType;

    private String appId;

    private double rating;

    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CatalogEntryType getAppType() {
        return appType;
    }

    public void setAppType(CatalogEntryType appType) {
        this.appType = appType;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
