package org.oasis_eu.portal.model.dashboard;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * User: schambon
 * Date: 4/16/15
 */
@Document(collection = "hidden_pending_apps")
public class HiddenPendingApps {
    @Id
    private String userId;

    @Field("apps")
    private List<String> hiddenApps = new ArrayList<>(0);

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getHiddenApps() {
        return hiddenApps;
    }

    public void setHiddenApps(List<String> hiddenApps) {
        this.hiddenApps = hiddenApps;
    }

    public void hideApp(String appId) {
        getHiddenApps().add(appId);
    }
}
