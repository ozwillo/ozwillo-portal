package org.oasis_eu.portal.model.store;


import java.util.Date;

public class OrganizationHistory implements Comparable<OrganizationHistory>{
    private String organizationId;
    private String name;
    private Date date;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(OrganizationHistory o) {
        return getDate().compareTo(o.getDate());
    }
}
