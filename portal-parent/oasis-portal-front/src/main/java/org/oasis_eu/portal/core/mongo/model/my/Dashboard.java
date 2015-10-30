package org.oasis_eu.portal.core.mongo.model.my;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * User: schambon
 * Date: 6/17/14
 */
@Document
public class Dashboard {

	@Id
	private String userId;

	private List<UserContext> contexts = new ArrayList<>();

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<UserContext> getContexts() {
		return contexts;
	}

	public void setContexts(List<UserContext> contexts) {
		this.contexts = contexts;
	}
}
