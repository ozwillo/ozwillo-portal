package org.oasis_eu.portal.model;



public class FormWidgetDate extends FormWidget {

	public FormWidgetDate(String id, String label) {
		super(id, label);
	}
	
	public String getType() {
		return "date";
	}

}
