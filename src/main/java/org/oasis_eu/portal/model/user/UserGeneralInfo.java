package org.oasis_eu.portal.model.user;

import org.oasis_eu.spring.kernel.model.Address;

public class UserGeneralInfo {

    String user_name;
    String user_email;
    String user_lastname;
    Address address;

    public UserGeneralInfo(String user_name, String user_lastname, String user_email, Address address) {
        this.user_name = user_name;
        this.user_lastname = user_lastname;
        this.user_email = user_email;
        this.address = address;
    }
}