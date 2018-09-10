package org.oasis_eu.portal.model.store;

import org.joda.time.DateTime;
import org.oasis_eu.portal.model.kernel.store.CatalogEntryType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * User: schambon
 * Date: 11/5/14
 */
@Document(collection = "appstore_install_status")
@CompoundIndexes({
    @CompoundIndex(def = "{catalogEntryType:1, catalogEntryId:1, userId:1}", unique = true)
})
public class InstalledStatus {

    @Id
    private String id;

    private CatalogEntryType catalogEntryType;
    private String catalogEntryId;
    private String userId;

    private boolean installed;

    // 24-hour validity so app install status will be recomputed at least once a day
    @Indexed(expireAfterSeconds = 86400)
    private DateTime computed = DateTime.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CatalogEntryType getCatalogEntryType() {
        return catalogEntryType;
    }

    public void setCatalogEntryType(CatalogEntryType catalogEntryType) {
        this.catalogEntryType = catalogEntryType;
    }

    public String getCatalogEntryId() {
        return catalogEntryId;
    }

    public void setCatalogEntryId(String catalogEntryId) {
        this.catalogEntryId = catalogEntryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public DateTime getComputed() {
        return computed;
    }

    public void setComputed(DateTime computed) {
        this.computed = computed;
    }
}

