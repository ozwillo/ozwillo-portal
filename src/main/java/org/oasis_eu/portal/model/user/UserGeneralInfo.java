package org.oasis_eu.portal.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.spring.kernel.model.Address;

public class UserGeneralInfo {

    @JsonProperty("user_name")
    String user_name;
    @JsonProperty("user_email")
    String user_email;
    @JsonProperty("user_lastname")
    String user_lastname;
    @JsonProperty("address")
    Address address;

    public UserGeneralInfo(String user_name, String user_lastname, String user_email, Address address) {
        this.user_name = user_name;
        this.user_lastname = user_lastname;
        this.user_email = user_email;
        this.address = address;
    }
}