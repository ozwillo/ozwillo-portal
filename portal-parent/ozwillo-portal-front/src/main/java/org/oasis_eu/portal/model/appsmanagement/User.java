package org.oasis_eu.portal.model.appsmanagement;

/**
* User: schambon
* Date: 8/14/14
*/
public class User implements Comparable<User> {
	String fullname;
	String userid;
	String email;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public User(String userid, String fullname, boolean admin) {
		this.fullname = fullname;
		this.userid = userid;
		this.admin = admin;

	}
	public User(String userid, String email, String fullname, boolean admin) {
		this.fullname = fullname;
		this.userid = userid;
		this.email = email;
		this.admin = admin;

	}

	public User() {
	}

	@Override
	public int compareTo(User user) {
		if (getFullname() == null)
			return -1;
		else if (user.getFullname() == null)
			return 1;
		else
			return getFullname().toLowerCase().compareTo(user.getFullname().toLowerCase());
	}
}
