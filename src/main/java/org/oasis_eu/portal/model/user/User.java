package org.oasis_eu.portal.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.oasis_eu.portal.config.CustomInstantDeserializer;
import org.oasis_eu.portal.config.CustomInstantSerializer;

import java.time.Instant;

/**
 * User: schambon
 * Date: 8/14/14
 */
public class User implements Comparable<User> {
    @JsonProperty("id")
    private String userid;

    private String name;

    private String email;

    @JsonSerialize(using = CustomInstantSerializer.class)
    @JsonDeserialize(using = CustomInstantDeserializer.class)
    private Instant created;

    boolean admin;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public User() { }

    public User(String userid, String name, boolean admin) {
        this.name = name;
        this.userid = userid;
        this.admin = admin;
    }

    public User(String userid, String email, String name, boolean admin) {
        this.name = name;
        this.userid = userid;
        this.email = email;
        this.admin = admin;
    }

    public User(String userid, String email, String name, Instant created, boolean admin) {
        this.name = name;
        this.userid = userid;
        this.email = email;
        this.created = created;
        this.admin = admin;
    }

    @Override
    public int compareTo(User user) {
        if (getName() == null)
            return -1;
        else if (user.getName() == null)
            return 1;
        else
            return getName().toLowerCase().compareTo(user.getName().toLowerCase());
    }
}
