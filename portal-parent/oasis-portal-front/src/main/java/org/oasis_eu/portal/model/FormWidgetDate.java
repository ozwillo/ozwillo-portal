package org.oasis_eu.portal.model;


import java.io.Serializable;

public class FormWidgetDate extends FormWidget implements Serializable {

	private static final long serialVersionUID = -2979104488824119110L;

	public FormWidgetDate(String id, String label) {
		super(id, label);
	}
	
	@Override
	public String getType() {
		return "date";
	}

}
