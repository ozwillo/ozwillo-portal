package org.oasis_eu.portal.model.history;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

public class UserOrganizationsHistory {
    @Id
    private String userId;

    private List<OrganizationHistory> organizationsHistory = new ArrayList<>();

    public UserOrganizationsHistory() {}

    public UserOrganizationsHistory(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrganizationHistory> getOrganizationsHistory() {
        return organizationsHistory;
    }

}