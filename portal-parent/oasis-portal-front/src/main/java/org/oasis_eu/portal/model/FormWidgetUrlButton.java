package org.oasis_eu.portal.model;

public class FormWidgetUrlButton extends FormWidget {

	private String buttonLabel;

	private String url;

	public FormWidgetUrlButton(String id, String label, String buttonLabel, String url) {
		super(id, label);
		this.buttonLabel = buttonLabel;
		this.url = url;
	}

	@Override
	public String getType() {
		
		return "urlButton";
	}
	
	public String getButtonLabel() {
		return buttonLabel;
	}

	public void setButtonLabel(String buttonLabel) {
		this.buttonLabel = buttonLabel;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
