package org.oasis_eu.portal.model.network;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.spring.kernel.model.OrganizationType;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * User: schambon
 * Date: 10/24/14
 */
public class UIOrganization {
    @JsonProperty
    @NotNull
    @NotEmpty
    String id;
    @JsonProperty
    @NotNull
    @NotEmpty
    String name;
    @JsonProperty
    @NotNull
    OrganizationType type;
    @JsonProperty
    boolean admin;

    @JsonProperty
    List<UIOrganizationMember> members = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrganizationType getType() {
        return type;
    }

    public void setType(OrganizationType type) {
        this.type = type;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public List<UIOrganizationMember> getMembers() {
        return members;
    }

    public void setMembers(List<UIOrganizationMember> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "{" +
                "id:'" + id + '\'' +
                ", name:'" + name + '\'' +
                ", type:" + type +
                ", admin:" + admin +
                ", members:" + members +
                '}';
    }
}
