package org.oasis_eu.portal.model.authority;

/**
 * User: schambon
 * Date: 8/8/14
 */
public class Authority {

    private AuthorityType type;
    private String name;
    private String id;
    private boolean admin;

    public Authority(AuthorityType type, String name, String id, boolean admin) {
        this.type = type;
        this.name = name;
        this.id = id;
        this.admin = admin;
    }

    public AuthorityType getType() {
        return type;
    }

    public void setType(AuthorityType type) {
        this.type = type;
    }

    public String getName() {
        return name != null ? name : "-";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
