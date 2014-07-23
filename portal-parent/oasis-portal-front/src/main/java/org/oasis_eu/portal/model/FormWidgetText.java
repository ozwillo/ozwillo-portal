package org.oasis_eu.portal.model;


public class FormWidgetText extends FormWidget {

	public FormWidgetText(String id, String label) {
		super(id, label);
	}
	
	public String getType() {
		return "text";
	}

}
