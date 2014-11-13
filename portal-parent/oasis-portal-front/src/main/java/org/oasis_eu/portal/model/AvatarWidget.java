package org.oasis_eu.portal.model;

import java.io.Serializable;
import java.util.List;

public class AvatarWidget extends FormWidget implements Serializable {

	private static final long serialVersionUID = 5069708302444653368L;
	private List<String> availableAvatars;
	
	public AvatarWidget(String id, String label) {
		super(id, label);
	}
	
	public AvatarWidget(String id, String label, List<String> avatars) {
		super(id, label);
		this.availableAvatars = avatars;
	}

	@Override
	public String getType() {
		
		return "avatar";
	}
	
	public List<String> getAvailableAvatars() {
		
		return availableAvatars;
	}

}
