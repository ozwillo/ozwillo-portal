package org.oasis_eu.portal.model.appsmanagement;

/**
 * User: schambon
 * Date: 8/8/14
 */
public class Authority {

    private AuthorityType type;
    private String name;
    private String id;

    public Authority(AuthorityType type, String name, String id) {
        this.type = type;
        this.name = name;
        this.id = id;
    }

    public AuthorityType getType() {
        return type;
    }

    public void setType(AuthorityType type) {
        this.type = type;
    }

    public String getName() {
        return name;
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


}
