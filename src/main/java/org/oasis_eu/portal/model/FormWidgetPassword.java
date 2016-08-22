package org.oasis_eu.portal.model;

import java.io.Serializable;

public class FormWidgetPassword extends FormWidget implements Serializable {

    private static final long serialVersionUID = 8476482644454253858L;

    private String urlLabel;
    private String url;

    public FormWidgetPassword(String id, String label, String urlLabel, String url) {
        super(id, label);
        this.urlLabel = urlLabel;
        this.url = url;
    }

    @Override
    public String getType() {
        return "password";
    }

    public String getUrlLabel() {
        return urlLabel;
    }

    public void setUrlLabel(String urlLabel) {
        this.urlLabel = urlLabel;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
