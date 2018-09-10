package org.oasis_eu.portal.model.history;


import java.util.Date;

public class OrganizationHistory implements Comparable<OrganizationHistory>{
    private String dcOrganizationId;
    private String name;
    private Date date;

    public OrganizationHistory() {}

    public OrganizationHistory(String dcOrganizationId, String name, Date date) {
        this.dcOrganizationId = dcOrganizationId;
        this.name = name;
        this.date = date;
    }

    public String getDcOrganizationId() {
        return dcOrganizationId;
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
