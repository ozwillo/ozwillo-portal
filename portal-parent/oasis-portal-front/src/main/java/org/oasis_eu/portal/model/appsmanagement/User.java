package org.oasis_eu.portal.model.appsmanagement;

/**
* User: schambon
* Date: 8/14/14
*/
public class User {
    String fullname;
    String userid;
    boolean admin;

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public User(String userid, String fullname, boolean admin) {
        this.fullname = fullname;
        this.userid = userid;
        this.admin = admin;

    }

    public User() {
    }
}
