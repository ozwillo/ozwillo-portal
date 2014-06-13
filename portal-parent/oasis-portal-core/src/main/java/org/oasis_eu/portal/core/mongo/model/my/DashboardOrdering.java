package org.oasis_eu.portal.core.mongo.model.my;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * User: schambon
 * Date: 6/12/14
 */
@Document
@CompoundIndexes({
        @CompoundIndex(name = "userId_userContextId", def = "{userId:1, userContextId:1}", unique = true)
})
public class DashboardOrdering  {

    @Id
    private String id;

    private String userId;
    private String userContextId;


    private Map<String, Integer> orderings = new HashMap<>();

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserContextId() {
        return userContextId;
    }

    public void setUserContextId(String userContextId) {
        this.userContextId = userContextId;
    }

    public void setOrderings(Map<String, Integer> orderings) {
        this.orderings = orderings;
    }

    public Map<String, Integer> getOrderings() {
        return orderings;
    }
}
