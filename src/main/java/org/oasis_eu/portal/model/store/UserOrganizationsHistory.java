package org.oasis_eu.portal.model.store;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

public class UserOrganizationsHistory {
    @Id
    private String userId;

    private List<OrganizationHistory> organizationsHistory = new ArrayList<OrganizationHistory>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrganizationHistory> getOrganizationsHistory() {
        return organizationsHistory;
    }

    public void setOrganizationsHistory(List<OrganizationHistory> organizationsHistory) {
        this.organizationsHistory = organizationsHistory;
    }


}